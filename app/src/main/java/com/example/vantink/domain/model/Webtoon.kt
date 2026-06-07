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
    val chapters: List<ChapterSummary> = emptyList(),
    val contentType: ContentType = ContentType.UNKNOWN,
    val readingMode: ReadingMode = ReadingMode.TOP_TO_BOTTOM,
    val language: String = "en"
)

data class ChapterSummary(
    val id: String,
    val title: String,
    val number: Float,
    val uploadDate: String = ""
)
