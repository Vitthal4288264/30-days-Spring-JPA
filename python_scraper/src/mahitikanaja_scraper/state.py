from __future__ import annotations

import json
from pathlib import Path


class ResumeState:
    def __init__(self, path: Path) -> None:
        self.path = path
        self.path.parent.mkdir(parents=True, exist_ok=True)
        if self.path.exists():
            self.data = json.loads(self.path.read_text())
        else:
            self.data = {"completed": []}

    def is_done(self, key: str) -> bool:
        return key in self.data["completed"]

    def mark_done(self, key: str) -> None:
        if key not in self.data["completed"]:
            self.data["completed"].append(key)
            self.path.write_text(json.dumps(self.data, indent=2))
