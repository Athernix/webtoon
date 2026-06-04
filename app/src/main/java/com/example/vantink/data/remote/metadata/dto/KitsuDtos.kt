package com.example.vantink.data.remote.metadata.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KitsuMangaResponse(
    val data: List<KitsuMangaData>
)

@JsonClass(generateAdapter = true)
data class KitsuMangaData(
    val id: String,
    val attributes: KitsuMangaAttributes
)

@JsonClass(generateAdapter = true)
data class KitsuMangaAttributes(
    val canonicalTitle: String,
    val synopsis: String?,
    val posterImage: KitsuImage?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class KitsuImage(
    val medium: String?,
    val large: String?,
    val original: String?
)
