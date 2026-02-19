from __future__ import annotations

import asyncio

from .config import build_parser, load_config
from .logging_config import setup_logging
from .scraper import MahitiKanajaScraper, dump_config_snapshot


def main() -> None:
    parser = build_parser()
    args = parser.parse_args()
    cfg = load_config(args)
    setup_logging()
    dump_config_snapshot(cfg)
    asyncio.run(MahitiKanajaScraper(cfg).run())


if __name__ == "__main__":
    main()
