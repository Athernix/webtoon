from __future__ import annotations

from pydantic import BaseModel, Field


class Extension(BaseModel):
    name: str
    pkgName: str
    baseUrl: str = ""
    lang: str
    version: str
    code_url: str = ""
    iconUrl: str = ""
    apkUrl: str = ""
    nsfw: bool = False


class SearchResult(BaseModel):
    id: str
    title: str
    thumbnailUrl: str = ""
    sourceName: str
    sourcePkgName: str
    url: str = ""


class ChapterItem(BaseModel):
    id: str
    title: str
    number: float = 0.0
    url: str


class ComicDetail(BaseModel):
    id: str
    title: str
    coverUrl: str = ""
    description: str = ""
    chapters: list[ChapterItem] = Field(default_factory=list)


class ChapterPages(BaseModel):
    id: str
    pages: list[str]
