from __future__ import annotations

import time
from urllib.parse import urljoin

import httpx

from .models import Extension
from .settings import Settings, get_settings


class KeiyoushiIndexService:
    def __init__(self, settings: Settings | None = None) -> None:
        self.settings = settings or get_settings()
        self._cache: list[Extension] = []
        self._cache_time = 0.0

    async def available_extensions(self, lang: str | None = None) -> list[Extension]:
        now = time.monotonic()
        if not self._cache or now - self._cache_time > self.settings.cache_ttl_seconds:
            self._cache = await self._fetch_index()
            self._cache_time = now

        if not lang:
            return self._cache

        requested = lang.lower()
        return [item for item in self._cache if item.lang.lower() == requested]

    async def get_extension(self, pkg_name: str) -> Extension | None:
        items = await self.available_extensions()
        return next((item for item in items if item.pkgName == pkg_name), None)

    async def _fetch_index(self) -> list[Extension]:
        async with httpx.AsyncClient(timeout=self.settings.request_timeout_seconds) as client:
            response = await client.get(self.settings.keiyoushi_index_url)
            response.raise_for_status()
            payload = response.json()

        return [item for raw in payload for item in self._normalize_entry(raw)]

    def _normalize_entry(self, raw: dict) -> list[Extension]:
        pkg_name = raw.get("pkg") or raw.get("pkgName") or ""
        lang = raw.get("lang") or raw.get("language") or "all"
        version = str(raw.get("version") or raw.get("code") or "0")
        apk = raw.get("apk") or ""
        apk_url = urljoin(f"{self.settings.keiyoushi_repo_base_url}/apk/", apk) if apk else ""
        icon_url = urljoin(f"{self.settings.keiyoushi_repo_base_url}/icon/", f"{apk.replace('.apk', '.png')}") if apk else ""
        code_url = raw.get("code_url") or raw.get("codeUrl") or raw.get("sourceUrl") or ""
        nsfw = bool(raw.get("nsfw") in (1, True, "1", "true"))
        sources = raw.get("sources") or [{}]

        normalized: list[Extension] = []
        for source in sources:
            source_name = source.get("name") or raw.get("name") or pkg_name
            source_base_url = source.get("baseUrl") or source.get("base_url") or raw.get("baseUrl") or ""
            normalized.append(
                Extension(
                    name=source_name,
                    pkgName=pkg_name,
                    baseUrl=source_base_url,
                    lang=lang,
                    version=version,
                    code_url=code_url,
                    iconUrl=source.get("iconUrl") or icon_url,
                    apkUrl=apk_url,
                    nsfw=nsfw,
                )
            )

        return normalized
