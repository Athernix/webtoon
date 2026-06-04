package com.example.vantink.data.mapper

import com.example.vantink.data.local.entity.FavoriteEntity
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.data.remote.dto.ChapterDto
import com.example.vantink.data.remote.dto.ChapterSummaryDto
import com.example.vantink.data.remote.dto.WebtoonDto
import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.Webtoon

fun WebtoonDto.toWebtoon(): Webtoon {
    return Webtoon(
        id = id,
        title = title,
        artist = artist ?: "",
        author = author ?: "",
        description = description ?: "",
        thumbnailUrl = thumbnailUrl ?: "",
        status = status ?: "Unknown",
        genres = genres ?: emptyList(),
        chapters = chapters?.map { it.toChapterSummary() } ?: emptyList()
    )
}

fun ChapterSummaryDto.toChapterSummary(): ChapterSummary {
    return ChapterSummary(
        id = id,
        title = title,
        number = number,
        uploadDate = uploadDate ?: ""
    )
}

fun ChapterDto.toChapter(): Chapter {
    return Chapter(
        id = id,
        webtoonId = webtoonId,
        title = title,
        number = number,
        pages = pages ?: emptyList(),
        uploadDate = uploadDate ?: ""
    )
}

fun Webtoon.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl
    )
}

fun FavoriteEntity.toWebtoon(): Webtoon {
    return Webtoon(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl
    )
}

// History mappers can be more specific since they involve current reading state
