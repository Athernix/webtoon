from __future__ import annotations

from functools import lru_cache
from pydantic import BaseModel


class Settings(BaseModel):
    keiyoushi_index_url: str = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json"
    keiyoushi_repo_base_url: str = "https://raw.githubusercontent.com/keiyoushi/extensions/repo"
    request_timeout_seconds: float = 18.0
    cache_ttl_seconds: int = 60 * 30


@lru_cache
def get_settings() -> Settings:
    return Settings()
