package com.example.vantink.data.remote.metadata.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JikanMangaResponse(
    @Json(name = "data") val data: List<JikanMangaData>
)

@JsonClass(generateAdapter = true)
data class JikanMangaData(
    @Json(name = "mal_id") val malId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "synopsis") val synopsis: String?,
    @Json(name = "images") val images: JikanImages,
    @Json(name = "status") val status: String?,
    @Json(name = "authors") val authors: List<JikanAuthor>?
)

@JsonClass(generateAdapter = true)
data class JikanImages(
    @Json(name = "jpg") val jpg: JikanJpg
)

@JsonClass(generateAdapter = true)
data class JikanJpg(
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "large_image_url") val largeImageUrl: String?
)

@JsonClass(generateAdapter = true)
data class JikanAuthor(
    @Json(name = "name") val name: String
)
