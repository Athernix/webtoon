package com.example.vantink.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExtensionDto(
    val name: String,
    val pkgName: String,
    val baseUrl: String = "",
    val lang: String,
    val version: String,
    @Json(name = "code_url") val codeUrl: String = "",
    val iconUrl: String = "",
    val apkUrl: String = "",
    val nsfw: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ExtensionSearchResultDto(
    val id: String,
    val title: String,
    val thumbnailUrl: String = "",
    val sourceName: String,
    val sourcePkgName: String,
    val url: String = ""
)

@JsonClass(generateAdapter = true)
data class ExtensionChapterDto(
    val id: String,
    val title: String,
    val number: Float = 0f,
    val url: String
)

@JsonClass(generateAdapter = true)
data class ExtensionComicDto(
    val id: String,
    val title: String,
    val coverUrl: String = "",
    val description: String = "",
    val chapters: List<ExtensionChapterDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ExtensionChapterPagesDto(
    val id: String,
    val pages: List<String> = emptyList()
)
