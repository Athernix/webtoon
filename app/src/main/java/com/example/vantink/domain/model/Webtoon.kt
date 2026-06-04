package com.example.vantink.domain.model

data class Webtoon(
    val id: String,
    val title: String,
    val artist: String = "",
    val author: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val status: String = "Unknown",
    val genres: List<String> = emptyList(),
    val chapters: List<ChapterSummary> = emptyList()
)

data class ChapterSummary(
    val id: String,
    val title: String,
    val number: Float,
    val uploadDate: String = ""
)
