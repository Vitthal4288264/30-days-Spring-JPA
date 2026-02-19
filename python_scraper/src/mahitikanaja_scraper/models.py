from __future__ import annotations

from dataclasses import asdict, dataclass, field


@dataclass(slots=True)
class SchemeRecord:
    department_name: str
    sub_department_name: str
    scheme_name: str
    scheme_description: str = ""
    eligibility_criteria: str = ""
    required_documents: str = ""
    benefits: str = ""
    district: str = ""
    taluka: str = ""
    application_mode: str = ""
    official_links: list[str] = field(default_factory=list)

    def dedupe_key(self) -> tuple[str, str, str, str, str]:
        return (
            self.department_name.casefold(),
            self.sub_department_name.casefold(),
            self.scheme_name.casefold(),
            self.district.casefold(),
            self.taluka.casefold(),
        )

    def to_dict(self) -> dict:
        data = asdict(self)
        data["official_links"] = list(dict.fromkeys(self.official_links))
        return data
