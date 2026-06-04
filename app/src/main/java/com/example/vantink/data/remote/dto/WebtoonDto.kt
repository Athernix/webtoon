package com.example.vantink.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WebtoonDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String?,
    @Json(name = "author") val author: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "genres") val genres: List<String>?,
    @Json(name = "chapters") val chapters: List<ChapterSummaryDto>?
)

@JsonClass(generateAdapter = true)
data class ChapterSummaryDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "number") val number: Float,
    @Json(name = "upload_date") val uploadDate: String?
)
