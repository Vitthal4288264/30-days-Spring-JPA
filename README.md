# GovPolicy Insight (Karnataka)

A simple web platform to collect and analyze publicly available government policy data, starting with Karnataka state.

## Goal
Build a website that:
- Fetches policy-related information from official public government sources.
- Tracks yearly policy counts and policy details.
- Validates each policy against existing acts/rules/laws to identify possible conflicts or compliance gaps.

## Problem We Are Solving
Policy information is often distributed across many portals and PDF documents. This project creates one searchable system for:
- Citizens
- Researchers
- Legal teams
- Policy analysts

## Scope (Phase 1)
- Focus state: **Karnataka**
- Focus domain: **Government policies (publicly available)**
- Output:
  - Year-wise policy count
  - Policy metadata (title, date, department, source URL)
  - Basic legal validation status (Valid / Needs Review / Conflict Suspected)

## High-Level Architecture

```text
[Government Public Websites / PDFs / Notifications]
                    |
                    v
          [Data Ingestion + Scraper Layer]
                    |
                    v
         [Parser + Normalization Service]
                    |
                    v
         [Policy Database (structured data)]
                    |
          +---------+---------+
          |                   |
          v                   v
 [Law Reference Database]   [Validation Engine]
          |                   |
          +---------+---------+
                    |
                    v
             [REST API Layer]
                    |
                    v
              [Web Dashboard]
```

## Proposed Components
1. **Scraper Service**
   - Pulls data from official websites.
   - Handles HTML pages, downloadable PDFs, and notices.

2. **Policy Processing Service**
   - Extracts key fields (title, date, department, summary, references).
   - Stores clean structured records.

3. **Law Validation Service**
   - Compares policy text/metadata with legal references.
   - Produces validation labels and notes.

4. **Backend API**
   - Exposes endpoints for filters (year, department, status).
   - Serves dashboard and external clients.

5. **Frontend Website**
   - Dashboard with yearly trends.
   - Policy list and detail view.
   - Validation status and reason notes.

## Initial Data Model (Simple)
- **Policy**
  - id
  - title
  - state
  - department
  - publication_date
  - year
  - source_url
  - summary
  - validation_status

- **LawReference**
  - id
  - law_name
  - section
  - jurisdiction
  - effective_date

- **ValidationResult**
  - id
  - policy_id
  - law_reference_id
  - result
  - remarks
  - checked_on

## Non-Functional Priorities
- Source traceability (every record should link to official source URL)
- Legal caution (results are advisory, not legal judgment)
- Extensible design for adding more states later

## Roadmap (Next Steps)
1. Finalize list of Karnataka official data sources.
2. Build a small scraper PoC for one department.
3. Define policy extraction template for PDF and HTML notices.
4. Create first API endpoints and a basic dashboard.
5. Add rule-based legal validation and review workflow.

## Disclaimer
This platform provides informational insights from publicly available data. Legal validation output is a decision-support aid and should be reviewed by legal experts before final use.

## Implemented Backend (Current)
This repository now includes a Spring Boot backend implementation for the Phase 1 scope:
- Policy, LawReference, and ValidationResult JPA entities
- In-memory H2 database setup with demo seed data
- REST endpoints for policy listing, filtering, yearly counts, law references, and validation results

### API Endpoints
- `GET /api/policies`
  - Optional query params: `year`, `department`, `status` (`VALID`, `NEEDS_REVIEW`, `CONFLICT_SUSPECTED`)
- `GET /api/policies/yearly-count`
- `GET /api/law-references`
- `GET /api/validation-results`
  - Optional query param: `policyId`

## Run Locally

### Prerequisites
1. **Java 21** (required by Gradle toolchain configuration in this project)
2. Internet access for the first Gradle dependency download

> You do **not** need to install Gradle manually because this repository includes the Gradle Wrapper (`./gradlew`).

### Steps
1. Clone and enter the project:
   ```bash
   git clone <your-repo-url>
   cd 30-days-Spring-JPA
   ```

2. Start the Spring Boot app:
   ```bash
   ./gradlew bootRun
   ```

3. Access the app and tooling:
   - API base: `http://localhost:8080/api`
   - H2 console: `http://localhost:8080/h2-console`

### Manual setup (keys / env variables / secrets)
- **No API keys are required** for the current implementation.
- **No environment variables are required** to run with the default in-memory H2 setup.
- If you later switch to an external database, you will need to provide datasource settings (for example `spring.datasource.url`, username, and password) through `application.properties` or environment variables.
