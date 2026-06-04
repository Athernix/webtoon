package com.example.vantink.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChapterDto(
    @Json(name = "id") val id: String,
    @Json(name = "webtoon_id") val webtoonId: String,
    @Json(name = "title") val title: String,
    @Json(name = "number") val number: Float,
    @Json(name = "pages") val pages: List<String>?,
    @Json(name = "upload_date") val uploadDate: String?
)
