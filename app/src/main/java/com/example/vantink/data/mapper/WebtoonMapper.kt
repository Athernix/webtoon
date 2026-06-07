package com.example.vantink.data.mapper

import com.example.vantink.data.local.entity.FavoriteEntity
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.data.remote.dto.ChapterDto
import com.example.vantink.data.remote.dto.ChapterSummaryDto
import com.example.vantink.data.remote.dto.WebtoonDto
import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.ContentType
import com.example.vantink.domain.model.ReadingMode
import com.example.vantink.domain.model.Webtoon

fun WebtoonDto.toWebtoon(contentType: ContentType = ContentType.UNKNOWN, language: String = "en"): Webtoon {
    val detectedType = if (contentType == ContentType.UNKNOWN)
        ContentType.fromTitle(title) else contentType

    return Webtoon(
        id = id,
        title = title,
        artist = artist ?: "",
        author = author ?: "",
        description = description ?: "",
        thumbnailUrl = thumbnailUrl ?: "",
        status = status ?: "Unknown",
        genres = genres ?: emptyList(),
        chapters = chapters?.map { it.toChapterSummary() } ?: emptyList(),
        contentType = detectedType,
        readingMode = detectedType.readingMode,
        language = language
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
        thumbnailUrl = thumbnailUrl,
        contentType = contentType.name,
        readingMode = readingMode.name,
        language = language
    )
}

fun FavoriteEntity.toWebtoon(): Webtoon {
    return Webtoon(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        contentType = try { ContentType.valueOf(contentType) } catch (e: Exception) { ContentType.UNKNOWN },
        readingMode = try { ReadingMode.valueOf(readingMode) } catch (e: Exception) { ReadingMode.TOP_TO_BOTTOM },
        language = language
    )
}

fun Webtoon.toHistoryEntity(
    chapterId: String,
    chapterTitle: String,
    chapterNumber: Float,
    scrollPosition: Int = 0
): HistoryEntity {
    return HistoryEntity(
        webtoonId = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        chapterNumber = chapterNumber,
        scrollPosition = scrollPosition,
        contentType = contentType.name,
        readingMode = readingMode.name,
        language = language
    )
}

fun HistoryEntity.toWebtoon(): Webtoon {
    return Webtoon(
        id = webtoonId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        contentType = try { ContentType.valueOf(contentType) } catch (e: Exception) { ContentType.UNKNOWN },
        readingMode = try { ReadingMode.valueOf(readingMode) } catch (e: Exception) { ReadingMode.TOP_TO_BOTTOM },
        language = language
    )
}
