from __future__ import annotations

import asyncio
import json
import logging
import urllib.robotparser
from pathlib import Path
from urllib.parse import urljoin

from tqdm.asyncio import tqdm

from .api_client import ApiClient
from .browser_client import BrowserDiscovery
from .config import ScraperConfig
from .models import SchemeRecord
from .parser import SchemeParser
from .rate_limiter import AsyncRateLimiter
from .state import ResumeState
from .storage import StorageManager
from .ua import UserAgentRotator

logger = logging.getLogger(__name__)


class MahitiKanajaScraper:
    def __init__(self, cfg: ScraperConfig) -> None:
        self.cfg = cfg
        self.state = ResumeState(cfg.output_dir / "resume_state.json")
        self.storage = StorageManager(cfg.output_dir, cfg.output_format, cfg.sqlite_path)
        self.ua_rotator = UserAgentRotator(cfg.user_agent_pool_path)
        self.limiter = AsyncRateLimiter(cfg.rate_limit_per_second)

    async def run(self) -> None:
        user_agent = self.ua_rotator.pick()
        self._assert_robots_allowed(user_agent)

        api = ApiClient(
            timeout=self.cfg.request_timeout,
            max_retries=self.cfg.max_retries,
            retry_backoff_seconds=self.cfg.retry_backoff_seconds,
            limiter=self.limiter,
            user_agent=user_agent,
        )

        try:
            async with BrowserDiscovery(self.cfg.base_url, self.cfg.headless, user_agent) as browser:
                await browser.open_home()
                sub_depts = await browser.discover_sub_departments()
                if self.cfg.department:
                    sub_depts = [x for x in sub_depts if self.cfg.department.casefold() in x[0].casefold()]

                for department, sub_department, sub_url in tqdm(sub_depts, desc="Sub-departments"):
                    await self._scrape_sub_department(browser, api, department, sub_department, sub_url)

                hints_path = self.cfg.output_dir / "api_hints.jsonl"
                hints_path.write_text("\n".join(browser.export_api_hints()), encoding="utf-8")
        finally:
            await api.close()

    def _assert_robots_allowed(self, user_agent: str) -> None:
        robots_url = urljoin(self.cfg.base_url.rstrip("/") + "/", "robots.txt")
        rp = urllib.robotparser.RobotFileParser()
        rp.set_url(robots_url)
        rp.read()
        if not rp.can_fetch(user_agent, self.cfg.base_url):
            raise PermissionError(f"Scraping blocked by robots.txt for {self.cfg.base_url}")

    async def _scrape_sub_department(self, browser: BrowserDiscovery, api: ApiClient, department: str, sub_department: str, sub_url: str) -> None:
        schemes = await browser.discover_schemes(sub_url)
        for scheme in tqdm(schemes, desc=f"Schemes:{sub_department}", leave=False):
            key = f"{department}|{sub_department}|{scheme.detail_url}"
            if self.state.is_done(key):
                continue

            record = await self._fetch_scheme_detail(api, department, sub_department, scheme.name, scheme.detail_url)
            if not record:
                continue

            districts, talukas = await browser.extract_dropdown_values(scheme.detail_url)
            districts = [d for d in districts if not self.cfg.district or self.cfg.district.casefold() in d.casefold()]
            await self._expand_geographies(record, districts, talukas)
            self.state.mark_done(key)

    async def _fetch_scheme_detail(
        self,
        api: ApiClient,
        department: str,
        sub_department: str,
        scheme_name: str,
        detail_url: str,
    ) -> SchemeRecord | None:
        # Prefer direct API calls where possible. If JSON isn't returned, fallback to HTML parsing.
        try:
            payload = await api.get_json(detail_url)
            if isinstance(payload, dict):
                return SchemeParser.from_api_payload(payload, department, sub_department)
            if isinstance(payload, list) and payload:
                return SchemeParser.from_api_payload(payload[0], department, sub_department)
        except Exception:
            logger.info("JSON endpoint not available for %s, parsing HTML", detail_url)

        try:
            html = await api.get_text(detail_url)
            return SchemeParser.from_scheme_detail_html(html, department, sub_department, scheme_name)
        except Exception as exc:
            logger.error("Failed scheme detail extraction for %s: %s", detail_url, exc)
            return None

    async def _expand_geographies(self, base_record: SchemeRecord, districts: list[str], talukas: list[str]) -> None:
        sem = asyncio.Semaphore(self.cfg.max_parallel_districts)

        async def _persist(district: str, taluka: str) -> None:
            async with sem:
                clone = SchemeRecord(**base_record.to_dict())
                clone.district = district
                clone.taluka = taluka
                self.storage.persist(clone)

        tasks = []
        if not districts:
            self.storage.persist(base_record)
            return

        for district in districts:
            if talukas:
                for taluka in talukas:
                    tasks.append(_persist(district, taluka))
            else:
                tasks.append(_persist(district, ""))

        await asyncio.gather(*tasks)


def dump_config_snapshot(cfg: ScraperConfig) -> None:
    snapshot = {
        "base_url": cfg.base_url,
        "headless": cfg.headless,
        "department": cfg.department,
        "district": cfg.district,
        "output_format": cfg.output_format,
    }
    (cfg.output_dir / "run_config.json").write_text(json.dumps(snapshot, indent=2), encoding="utf-8")
