from __future__ import annotations

from fastapi import FastAPI, HTTPException, Query

from .keiyoushi_service import KeiyoushiIndexService
from .models import ChapterPages, ComicDetail, Extension, SearchResult
from .selector_engine import SelectorEngine

app = FastAPI(title="VantInk Keiyoushi Adapter", version="1.0.0")
index_service = KeiyoushiIndexService()
selector_engine = SelectorEngine()


@app.get("/api/v1/extensions/available", response_model=list[Extension])
async def available_extensions(lang: str | None = Query(default=None)) -> list[Extension]:
    return await index_service.available_extensions(lang=lang)


@app.get("/api/v1/source/{pkg_name}/search", response_model=list[SearchResult])
async def search_source(pkg_name: str, q: str = Query(min_length=1)) -> list[SearchResult]:
    extension = await _extension_or_404(pkg_name)
    if not extension.baseUrl:
        return []
    return await selector_engine.search(extension, q)


@app.get("/api/v1/source/{pkg_name}/comic/{comic_id:path}", response_model=ComicDetail)
async def comic_detail(pkg_name: str, comic_id: str) -> ComicDetail:
    extension = await _extension_or_404(pkg_name)
    return await selector_engine.comic(extension, comic_id)


@app.get("/api/v1/source/{pkg_name}/chapter/{chapter_id:path}", response_model=ChapterPages)
async def chapter_pages(pkg_name: str, chapter_id: str) -> ChapterPages:
    extension = await _extension_or_404(pkg_name)
    return await selector_engine.chapter(extension, chapter_id)


async def _extension_or_404(pkg_name: str) -> Extension:
    extension = await index_service.get_extension(pkg_name)
    if not extension:
        raise HTTPException(status_code=404, detail=f"Unknown Keiyoushi source: {pkg_name}")
    return extension
