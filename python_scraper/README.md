# Mahiti Kanaja Playwright Scraper

Production-oriented async scraper for https://mahitikanaja.karnataka.gov.in.

## Features
- Async Playwright navigation for SPA content and dropdown handling.
- Network/API discovery: records API-like responses and tries JSON endpoints first.
- `httpx` direct API fallback path with retries and exponential-style backoff.
- Robots.txt compliance check before scraping.
- Rate limiting and user-agent rotation.
- Resume support with `resume_state.json`.
- Parallel district/taluka expansion with semaphore control.
- Data deduplication (in-memory + SQLite unique constraints).
- JSON/CSV outputs and optional SQLite persistence.
- Structured logging.
- CLI filtering by department and district.

## Project structure

```text
python_scraper/
  src/mahitikanaja_scraper/
    cli.py
    config.py
    scraper.py
    browser_client.py
    api_client.py
    parser.py
    storage.py
    state.py
    ua.py
    rate_limiter.py
    models.py
    logging_config.py
  requirements.txt
  .env.example
  Dockerfile
  user_agents.txt
```

## Setup

```bash
cd python_scraper
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
playwright install chromium
cp .env.example .env
```

## Run

```bash
PYTHONPATH=src python -m mahitikanaja_scraper.cli --headless --output-format both
```

Optional filters:

```bash
PYTHONPATH=src python -m mahitikanaja_scraper.cli --department "Agriculture" --district "Bengaluru" --output-format json
```

## Output layout

```text
data/
  <department_name>/
    <scheme_name>.json
    <scheme_name>.csv
  schemes.db
  api_hints.jsonl
  resume_state.json
  run_config.json
```

## Dynamic DOM handling notes
- The scraper uses `wait_for_load_state("networkidle")` before querying cards and links.
- It probes multiple selector patterns (`.scheme-card a`, URL patterns, label-based selectors) because SPA templates can vary by sub-department.
- Pagination is handled by iterating a `Next` locator until disabled.
- District/taluka values are extracted from dynamic `<select>` option lists and expanded in parallel.

## API-first strategy
- All scheme detail URLs are first attempted through `httpx` JSON calls.
- If JSON parsing fails, scraper falls back to HTML parser.
- Captured API-like responses are saved to `api_hints.jsonl` to speed payload reverse-engineering and replacement with direct API calls.

## Auth/captcha limitations
- If captcha or authenticated sessions are required, non-interactive bypass is intentionally not attempted.
- In that case, use authenticated API tokens/cookies provided by platform operators.
