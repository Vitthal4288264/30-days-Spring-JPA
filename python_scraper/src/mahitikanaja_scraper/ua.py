from __future__ import annotations

import random
from pathlib import Path

DEFAULT_USER_AGENTS = [
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_3_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
]


class UserAgentRotator:
    def __init__(self, user_agent_file: Path | None = None) -> None:
        self._pool = DEFAULT_USER_AGENTS.copy()
        if user_agent_file and user_agent_file.exists():
            custom = [line.strip() for line in user_agent_file.read_text().splitlines() if line.strip()]
            if custom:
                self._pool = custom

    def pick(self) -> str:
        return random.choice(self._pool)
