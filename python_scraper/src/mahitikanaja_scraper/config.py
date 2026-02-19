from __future__ import annotations

import argparse
import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv


@dataclass(slots=True)
class ScraperConfig:
    base_url: str
    output_dir: Path
    headless: bool
    department: str | None
    district: str | None
    output_format: str
    request_timeout: float
    max_retries: int
    retry_backoff_seconds: float
    rate_limit_per_second: float
    max_parallel_districts: int
    sqlite_path: Path | None
    user_agent_pool_path: Path | None


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Mahiti Kanaja scraper")
    parser.add_argument("--department", help="Scrape only one department", default=None)
    parser.add_argument("--district", help="Scrape only one district", default=None)
    parser.add_argument("--headless", action="store_true", help="Run browser in headless mode")
    parser.add_argument(
        "--output-format",
        choices=["json", "csv", "both"],
        default="both",
        help="Output data format",
    )
    parser.add_argument("--output-dir", default="data", help="Output root folder")
    parser.add_argument("--sqlite-path", default="data/schemes.db", help="SQLite database path")
    parser.add_argument("--max-parallel-districts", type=int, default=4)
    return parser


def load_config(args: argparse.Namespace) -> ScraperConfig:
    load_dotenv()
    output_dir = Path(args.output_dir)
    sqlite_path = Path(args.sqlite_path) if args.sqlite_path else None
    return ScraperConfig(
        base_url=os.getenv("BASE_URL", "https://mahitikanaja.karnataka.gov.in"),
        output_dir=output_dir,
        headless=args.headless or os.getenv("HEADLESS", "true").lower() == "true",
        department=args.department,
        district=args.district,
        output_format=args.output_format,
        request_timeout=float(os.getenv("REQUEST_TIMEOUT", "30")),
        max_retries=int(os.getenv("MAX_RETRIES", "3")),
        retry_backoff_seconds=float(os.getenv("RETRY_BACKOFF_SECONDS", "1.5")),
        rate_limit_per_second=float(os.getenv("RATE_LIMIT_PER_SECOND", "2.0")),
        max_parallel_districts=args.max_parallel_districts,
        sqlite_path=sqlite_path,
        user_agent_pool_path=Path(os.getenv("USER_AGENT_FILE", "user_agents.txt")),
    )
