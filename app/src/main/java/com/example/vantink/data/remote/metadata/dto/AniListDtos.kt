package com.example.vantink.data.remote.metadata.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AniListRequest(
    val query: String,
    val variables: Map<String, Any?>
)

@JsonClass(generateAdapter = true)
data class AniListResponse(
    val data: AniListData? = null,
    val errors: List<AniListError>? = null
)

@JsonClass(generateAdapter = true)
data class AniListError(
    val message: String
)

@JsonClass(generateAdapter = true)
data class AniListData(
    @Json(name = "Page") val page: PageData? = null
)

@JsonClass(generateAdapter = true)
data class PageData(
    val pageInfo: PageInfo? = null,
    val media: List<AniListMedia>? = null
)

@JsonClass(generateAdapter = true)
data class PageInfo(
    val total: Int?,
    val hasNextPage: Boolean?
)

@JsonClass(generateAdapter = true)
data class AniListMedia(
    val id: Int,
    val title: AniListTitle,
    val description: String?,
    val coverImage: AniListCoverImage,
    val bannerImage: String?,
    val genres: List<String>?,
    val averageScore: Int?,
    val status: String?,
    val format: String?
)

@JsonClass(generateAdapter = true)
data class AniListTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

@JsonClass(generateAdapter = true)
data class AniListCoverImage(
    val large: String,
    val extraLarge: String?
)
