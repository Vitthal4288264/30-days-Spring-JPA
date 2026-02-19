from __future__ import annotations

import asyncio
import logging
from typing import Any

import httpx

from .rate_limiter import AsyncRateLimiter

logger = logging.getLogger(__name__)


class ApiClient:
    def __init__(
        self,
        timeout: float,
        max_retries: int,
        retry_backoff_seconds: float,
        limiter: AsyncRateLimiter,
        user_agent: str,
    ) -> None:
        self.max_retries = max_retries
        self.retry_backoff_seconds = retry_backoff_seconds
        self.limiter = limiter
        self.client = httpx.AsyncClient(
            timeout=timeout,
            headers={"User-Agent": user_agent, "Accept": "application/json, text/html"},
        )

    async def get_json(self, url: str, params: dict[str, Any] | None = None) -> Any:
        for attempt in range(1, self.max_retries + 1):
            try:
                await self.limiter.wait()
                response = await self.client.get(url, params=params)
                response.raise_for_status()
                return response.json()
            except Exception as exc:
                if attempt == self.max_retries:
                    raise
                sleep_for = self.retry_backoff_seconds * attempt
                logger.warning("Retrying %s after error: %s", url, exc)
                await asyncio.sleep(sleep_for)

    async def get_text(self, url: str, params: dict[str, Any] | None = None) -> str:
        for attempt in range(1, self.max_retries + 1):
            try:
                await self.limiter.wait()
                response = await self.client.get(url, params=params)
                response.raise_for_status()
                return response.text
            except Exception as exc:
                if attempt == self.max_retries:
                    raise
                sleep_for = self.retry_backoff_seconds * attempt
                logger.warning("Retrying %s after error: %s", url, exc)
                await asyncio.sleep(sleep_for)

    async def close(self) -> None:
        await self.client.aclose()
