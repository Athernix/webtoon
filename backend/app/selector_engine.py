from __future__ import annotations

import json
from pathlib import Path
from urllib.parse import quote_plus, urljoin

import httpx
from bs4 import BeautifulSoup

from .models import ChapterItem, ChapterPages, ComicDetail, Extension, SearchResult


DEFAULT_PROFILE = {
    "search_path": "/?s={query}",
    "result": {
        "item": "article, .post, .page-item-detail, .bsx, .manga",
        "title": "h3 a, h2 a, .post-title a, a[title]",
        "url": "h3 a, h2 a, .post-title a, a[title]",
        "thumbnail": "img",
    },
    "comic": {
        "title": "h1, .post-title h1, .entry-title",
        "cover": ".summary_image img, .thumb img, img.wp-post-image",
        "description": ".description-summary, .summary__content, .entry-content",
        "chapter": ".wp-manga-chapter a, .chapter-list a, .eph-num a, .listing-chapters_wrap a",
    },
    "chapter": {
        "image": ".reading-content img, .page-break img, .chapter-content img, img",
    },
}


class SelectorEngine:
    def __init__(self, profile_path: Path | None = None) -> None:
        self.profile_path = profile_path or Path(__file__).with_name("extension_selectors.json")
        self._profiles = self._load_profiles()

    def _load_profiles(self) -> dict:
        if not self.profile_path.exists():
            return {}
        return json.loads(self.profile_path.read_text(encoding="utf-8"))

    def _profile_for(self, pkg_name: str) -> dict:
        profile = dict(DEFAULT_PROFILE)
        profile.update(self._profiles.get(pkg_name, {}))
        return profile

    async def search(self, extension: Extension, query: str) -> list[SearchResult]:
        profile = self._profile_for(extension.pkgName)
        search_path = profile["search_path"].format(query=quote_plus(query))
        url = urljoin(extension.baseUrl.rstrip("/") + "/", search_path.lstrip("/"))
        soup = await self._soup(url)
        result_profile = profile["result"]

        results: list[SearchResult] = []
        for item in soup.select(result_profile["item"])[:30]:
            title_node = item.select_one(result_profile["title"])
            url_node = item.select_one(result_profile["url"])
            image_node = item.select_one(result_profile["thumbnail"])
            href = url_node.get("href", "") if url_node else ""
            if title_node and href:
                results.append(
                    SearchResult(
                        id=href,
                        title=title_node.get_text(" ", strip=True) or title_node.get("title", ""),
                        thumbnailUrl=self._image_url(image_node, extension.baseUrl),
                        sourceName=extension.name,
                        sourcePkgName=extension.pkgName,
                        url=href,
                    )
                )
        return results

    async def comic(self, extension: Extension, comic_id: str) -> ComicDetail:
        profile = self._profile_for(extension.pkgName)["comic"]
        url = self._absolute_url(comic_id, extension.baseUrl)
        soup = await self._soup(url)
        title_node = soup.select_one(profile["title"])
        cover_node = soup.select_one(profile["cover"])
        description_node = soup.select_one(profile["description"])
        chapters = []
        for index, chapter in enumerate(soup.select(profile["chapter"])):
            href = chapter.get("href", "")
            if href:
                chapters.append(
                    ChapterItem(
                        id=href,
                        title=chapter.get_text(" ", strip=True),
                        number=float(len(chapters) + 1),
                        url=href,
                    )
                )
        chapters.reverse()
        return ComicDetail(
            id=comic_id,
            title=title_node.get_text(" ", strip=True) if title_node else comic_id,
            coverUrl=self._image_url(cover_node, extension.baseUrl),
            description=description_node.get_text(" ", strip=True) if description_node else "",
            chapters=chapters,
        )

    async def chapter(self, extension: Extension, chapter_id: str) -> ChapterPages:
        profile = self._profile_for(extension.pkgName)["chapter"]
        url = self._absolute_url(chapter_id, extension.baseUrl)
        soup = await self._soup(url)
        pages = [
            self._image_url(image, extension.baseUrl)
            for image in soup.select(profile["image"])
            if self._image_url(image, extension.baseUrl)
        ]
        return ChapterPages(id=chapter_id, pages=list(dict.fromkeys(pages)))

    async def _soup(self, url: str) -> BeautifulSoup:
        async with httpx.AsyncClient(timeout=20.0, follow_redirects=True) as client:
            response = await client.get(url, headers={"User-Agent": "Mozilla/5.0 VantInk/1.0"})
            response.raise_for_status()
        return BeautifulSoup(response.text, "html.parser")

    def _absolute_url(self, value: str, base_url: str) -> str:
        if value.startswith("http://") or value.startswith("https://"):
            return value
        return urljoin(base_url.rstrip("/") + "/", value.lstrip("/"))

    def _image_url(self, node, base_url: str) -> str:
        if not node:
            return ""
        src = node.get("data-src") or node.get("data-lazy-src") or node.get("src") or ""
        return self._absolute_url(src, base_url) if src else ""
