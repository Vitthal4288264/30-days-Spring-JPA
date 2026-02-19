from __future__ import annotations

import json
import logging
from dataclasses import dataclass

from playwright.async_api import Browser, BrowserContext, Page, async_playwright

logger = logging.getLogger(__name__)


@dataclass(slots=True)
class SchemeHandle:
    name: str
    detail_url: str


class BrowserDiscovery:
    def __init__(self, base_url: str, headless: bool, user_agent: str) -> None:
        self.base_url = base_url
        self.headless = headless
        self.user_agent = user_agent
        self._playwright = None
        self._browser: Browser | None = None
        self._context: BrowserContext | None = None
        self.page: Page | None = None
        self.captured_api_calls: list[tuple[str, str | None]] = []

    async def __aenter__(self) -> "BrowserDiscovery":
        self._playwright = await async_playwright().start()
        self._browser = await self._playwright.chromium.launch(headless=self.headless)
        self._context = await self._browser.new_context(user_agent=self.user_agent)
        self.page = await self._context.new_page()

        async def _capture(response):
            url = response.url
            if "/api" in url.lower() or "scheme" in url.lower() or "department" in url.lower():
                body = None
                try:
                    body = await response.text() if "application/json" in response.headers.get("content-type", "") else None
                except Exception:
                    body = None
                self.captured_api_calls.append((url, body))

        self.page.on("response", _capture)
        return self

    async def __aexit__(self, exc_type, exc, tb) -> None:
        if self._context:
            await self._context.close()
        if self._browser:
            await self._browser.close()
        if self._playwright:
            await self._playwright.stop()

    async def open_home(self) -> None:
        assert self.page
        await self.page.goto(self.base_url, wait_until="domcontentloaded")
        await self.page.wait_for_load_state("networkidle")

    async def discover_sub_departments(self) -> list[tuple[str, str, str]]:
        """Return tuples of (department, sub_department, url)."""
        assert self.page
        results: list[tuple[str, str, str]] = []

        # Dynamic DOM note: many SPA pages only render cards after JS API calls complete,
        # so we wait for network idle and query multiple candidate selectors.
        await self.page.wait_for_load_state("networkidle")
        selectors = ["a[href*='subdepartment']", ".sub-department a", "a:has-text('Sub Department')"]
        handles = []
        for selector in selectors:
            handles = await self.page.query_selector_all(selector)
            if handles:
                break

        for a in handles:
            title = (await a.inner_text()).strip() or "Unknown"
            href = await a.get_attribute("href") or ""
            if href and not href.startswith("http"):
                href = self.base_url.rstrip("/") + "/" + href.lstrip("/")
            department = "Unknown Department"
            results.append((department, title, href))
        return list({(d, s, u) for d, s, u in results if u})

    async def discover_schemes(self, sub_department_url: str) -> list[SchemeHandle]:
        assert self.page
        await self.page.goto(sub_department_url, wait_until="domcontentloaded")
        await self.page.wait_for_load_state("networkidle")
        schemes: list[SchemeHandle] = []

        next_button = self.page.locator("button:has-text('Next'), a:has-text('Next')")
        while True:
            items = self.page.locator("a[href*='scheme'], .scheme-card a, .scheme-list a")
            count = await items.count()
            for idx in range(count):
                node = items.nth(idx)
                name = (await node.inner_text()).strip() or f"Scheme-{idx + 1}"
                href = await node.get_attribute("href") or ""
                if href and not href.startswith("http"):
                    href = self.base_url.rstrip("/") + "/" + href.lstrip("/")
                if href:
                    schemes.append(SchemeHandle(name=name, detail_url=href))

            if await next_button.count() and await next_button.first.is_enabled():
                await next_button.first.click()
                await self.page.wait_for_load_state("networkidle")
            else:
                break
        deduped = {(s.name, s.detail_url): s for s in schemes}
        return list(deduped.values())

    async def extract_dropdown_values(self, scheme_url: str) -> tuple[list[str], list[str]]:
        assert self.page
        await self.page.goto(scheme_url, wait_until="domcontentloaded")
        await self.page.wait_for_load_state("networkidle")

        districts = []
        talukas = []
        district_select = self.page.locator("select[name*='district'], select#district")
        taluka_select = self.page.locator("select[name*='taluka'], select[name*='taluk'], select#taluka")

        if await district_select.count():
            options = district_select.first.locator("option")
            for i in range(await options.count()):
                text = (await options.nth(i).inner_text()).strip()
                if text and "select" not in text.lower():
                    districts.append(text)
        if await taluka_select.count():
            options = taluka_select.first.locator("option")
            for i in range(await options.count()):
                text = (await options.nth(i).inner_text()).strip()
                if text and "select" not in text.lower():
                    talukas.append(text)

        return districts, talukas

    def export_api_hints(self) -> list[str]:
        hints = []
        for url, body in self.captured_api_calls:
            entry = {"url": url}
            if body:
                try:
                    entry["sample"] = json.loads(body)
                except Exception:
                    entry["sample"] = body[:500]
            hints.append(json.dumps(entry, ensure_ascii=False))
        return hints
