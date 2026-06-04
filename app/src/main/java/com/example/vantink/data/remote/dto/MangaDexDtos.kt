package com.example.vantink.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MangaDexResponse<T>(
    val result: String,
    val response: String,
    val data: T
)

@JsonClass(generateAdapter = true)
data class MangaDexListResponse<T>(
    val result: String,
    val response: String,
    val data: List<T>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

@JsonClass(generateAdapter = true)
data class MangaDexManga(
    val id: String,
    val type: String,
    val attributes: MangaDexMangaAttributes,
    val relationships: List<MangaDexRelationship>
)

@JsonClass(generateAdapter = true)
data class MangaDexMangaAttributes(
    val title: Map<String, String>,
    val description: Map<String, String>?,
    val status: String?,
    val contentRating: String?
)

@JsonClass(generateAdapter = true)
data class MangaDexRelationship(
    val id: String,
    val type: String,
    val attributes: Map<String, Any?>?
)

@JsonClass(generateAdapter = true)
data class MangaDexChapter(
    val id: String,
    val type: String,
    val attributes: MangaDexChapterAttributes
)

@JsonClass(generateAdapter = true)
data class MangaDexChapterAttributes(
    val volume: String?,
    val chapter: String?,
    val title: String?,
    val translatedLanguage: String?,
    val pages: Int
)

@JsonClass(generateAdapter = true)
data class MangaDexAtHomeResponse(
    val result: String,
    val baseUrl: String,
    val chapter: MangaDexAtHomeChapter
)

@JsonClass(generateAdapter = true)
data class MangaDexAtHomeChapter(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>
)
