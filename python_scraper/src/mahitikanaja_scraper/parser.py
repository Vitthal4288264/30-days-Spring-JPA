from __future__ import annotations

from collections.abc import Mapping

from bs4 import BeautifulSoup

from .models import SchemeRecord


class SchemeParser:
    @staticmethod
    def from_api_payload(payload: Mapping, department: str, sub_department: str) -> SchemeRecord:
        return SchemeRecord(
            department_name=department,
            sub_department_name=sub_department,
            scheme_name=str(payload.get("schemeName") or payload.get("name") or "Unknown Scheme"),
            scheme_description=str(payload.get("description") or ""),
            eligibility_criteria=str(payload.get("eligibility") or payload.get("eligibilityCriteria") or ""),
            required_documents=str(payload.get("documents") or payload.get("requiredDocuments") or ""),
            benefits=str(payload.get("benefits") or ""),
            district=str(payload.get("district") or ""),
            taluka=str(payload.get("taluka") or payload.get("taluk") or ""),
            application_mode=str(payload.get("applicationMode") or ""),
            official_links=[str(link) for link in payload.get("officialLinks", []) if link],
        )

    @staticmethod
    def from_scheme_detail_html(html: str, department: str, sub_department: str, scheme_name: str) -> SchemeRecord:
        soup = BeautifulSoup(html, "html.parser")

        def by_label(label: str) -> str:
            lbl = soup.find(string=lambda s: s and label.lower() in s.strip().lower())
            if not lbl:
                return ""
            parent = lbl.find_parent()
            if not parent:
                return ""
            sibling_text = " ".join(parent.stripped_strings)
            return sibling_text.replace(label, "").strip(" :")

        links = [a.get("href") for a in soup.select("a[href]") if a.get("href")]
        return SchemeRecord(
            department_name=department,
            sub_department_name=sub_department,
            scheme_name=scheme_name,
            scheme_description=by_label("Description"),
            eligibility_criteria=by_label("Eligibility"),
            required_documents=by_label("Documents"),
            benefits=by_label("Benefits"),
            application_mode=by_label("Application Mode"),
            official_links=links,
        )
