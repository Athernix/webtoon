package com.example.vantink.domain.model

enum class ContentType(val displayName: String, val readingMode: ReadingMode) {
    MANGA("Manga", ReadingMode.RIGHT_TO_LEFT),
    MANWHA("Manwha", ReadingMode.TOP_TO_BOTTOM),
    MANHUA("Manhua", ReadingMode.LEFT_TO_RIGHT),
    WEBTOON("Webtoon", ReadingMode.TOP_TO_BOTTOM),
    UNKNOWN("Unknown", ReadingMode.TOP_TO_BOTTOM);

    companion object {
        fun fromLanguageCode(code: String?): ContentType {
            return when (code?.lowercase()) {
                "ko" -> MANWHA
                "zh", "zh-cn", "zh-tw" -> MANHUA
                "ja", "jp" -> MANGA
                else -> UNKNOWN
            }
        }

        fun fromTitle(title: String): ContentType {
            return when {
                title.contains("webtoon", ignoreCase = true) -> WEBTOON
                title.contains("manwha", ignoreCase = true) -> MANWHA
                title.contains("manhua", ignoreCase = true) -> MANHUA
                title.contains("manga", ignoreCase = true) -> MANGA
                else -> UNKNOWN
            }
        }
    }
}

enum class ReadingMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    TOP_TO_BOTTOM
}

