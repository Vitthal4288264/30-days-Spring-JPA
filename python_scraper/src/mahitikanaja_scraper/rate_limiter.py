from __future__ import annotations

import asyncio
import time


class AsyncRateLimiter:
    def __init__(self, per_second: float) -> None:
        self.interval = 1 / max(per_second, 0.1)
        self._lock = asyncio.Lock()
        self._last = 0.0

    async def wait(self) -> None:
        async with self._lock:
            now = time.monotonic()
            delta = now - self._last
            if delta < self.interval:
                await asyncio.sleep(self.interval - delta)
            self._last = time.monotonic()
