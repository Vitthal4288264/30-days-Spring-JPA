from __future__ import annotations

import csv
import json
import sqlite3
from pathlib import Path

from .models import SchemeRecord


class StorageManager:
    def __init__(self, output_dir: Path, output_format: str, sqlite_path: Path | None) -> None:
        self.output_dir = output_dir
        self.output_format = output_format
        self.sqlite_path = sqlite_path
        self._seen: set[tuple[str, str, str, str, str]] = set()
        self.output_dir.mkdir(parents=True, exist_ok=True)
        if self.sqlite_path:
            self.sqlite_path.parent.mkdir(parents=True, exist_ok=True)
            self._init_db()

    def _init_db(self) -> None:
        with sqlite3.connect(self.sqlite_path) as conn:
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS schemes (
                    department_name TEXT,
                    sub_department_name TEXT,
                    scheme_name TEXT,
                    scheme_description TEXT,
                    eligibility_criteria TEXT,
                    required_documents TEXT,
                    benefits TEXT,
                    district TEXT,
                    taluka TEXT,
                    application_mode TEXT,
                    official_links TEXT,
                    UNIQUE(department_name, sub_department_name, scheme_name, district, taluka)
                )
                """
            )
            conn.commit()

    def persist(self, record: SchemeRecord) -> None:
        key = record.dedupe_key()
        if key in self._seen:
            return
        self._seen.add(key)

        department_folder = self.output_dir / self._safe(record.department_name)
        department_folder.mkdir(parents=True, exist_ok=True)
        base = department_folder / self._safe(record.scheme_name)
        row = record.to_dict()

        if self.output_format in {"json", "both"}:
            with base.with_suffix(".json").open("a", encoding="utf-8") as fh:
                fh.write(json.dumps(row, ensure_ascii=False) + "\n")

        if self.output_format in {"csv", "both"}:
            csv_path = base.with_suffix(".csv")
            write_header = not csv_path.exists()
            with csv_path.open("a", newline="", encoding="utf-8") as fh:
                writer = csv.DictWriter(fh, fieldnames=row.keys())
                if write_header:
                    writer.writeheader()
                writer.writerow({**row, "official_links": "|".join(row["official_links"])})

        if self.sqlite_path:
            with sqlite3.connect(self.sqlite_path) as conn:
                conn.execute(
                    """
                    INSERT OR IGNORE INTO schemes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    (
                        record.department_name,
                        record.sub_department_name,
                        record.scheme_name,
                        record.scheme_description,
                        record.eligibility_criteria,
                        record.required_documents,
                        record.benefits,
                        record.district,
                        record.taluka,
                        record.application_mode,
                        json.dumps(record.official_links, ensure_ascii=False),
                    ),
                )
                conn.commit()

    @staticmethod
    def _safe(value: str) -> str:
        return "".join(ch if ch.isalnum() or ch in {"-", "_"} else "_" for ch in value).strip("_") or "unknown"
