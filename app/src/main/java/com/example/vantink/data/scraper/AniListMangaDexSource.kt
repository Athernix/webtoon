package com.example.vantink.data.scraper

import android.util.Log
import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService
import com.example.vantink.data.remote.metadata.dto.AniListRequest
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.ContentType
import com.example.vantink.domain.model.ReadingMode
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AniListMangaDexSource(
    private val aniListApi: AniListApiService,
    private val mangaDexApi: MangaDexApiService
) : Source {
    override val name: String = "AniList + MangaDex"
    override val baseUrl: String = "https://anilist.co"

    private val TAG = "AniListMangaDexSource"

    private val aniListQuery = """
        query (${'$'}search: String, ${'$'}genres: [String], ${'$'}tags: [String], ${'$'}status: MediaStatus, ${'$'}format: MediaFormat, ${'$'}id: Int, ${'$'}sort: [MediaSort], ${'$'}page: Int, ${'$'}perPage: Int) {
          Page (page: ${'$'}page, perPage: ${'$'}perPage) {
            pageInfo { total hasNextPage }
            media (search: ${'$'}search, genre_in: ${'$'}genres, tag_in: ${'$'}tags, status: ${'$'}status, format: ${'$'}format, id: ${'$'}id, sort: ${'$'}sort, type: MANGA) {
              id
              title { english romaji native }
              description
              coverImage { large extraLarge }
              genres
              averageScore
              status
              format
            }
          }
        }
    """.trimIndent()

    override suspend fun searchWebtoons(filter: com.example.vantink.domain.model.SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            val variables = mutableMapOf<String, Any?>()

            if (filter.query.isNotBlank()) variables["search"] = filter.query
            if (filter.genres.isNotEmpty()) variables["genres"] = filter.genres
            if (filter.tags.isNotEmpty()) variables["tags"] = filter.tags
            if (filter.status != null) variables["status"] = filter.status
            if (filter.format != null) variables["format"] = filter.format

            variables["sort"] = listOf(filter.sort)
            variables["page"] = filter.page
            variables["perPage"] = filter.perPage

            val response = aniListApi.postQuery(AniListRequest(aniListQuery, variables))

            if (response.errors != null && response.data?.page == null) {
                if (filter.query.isNotBlank() && (filter.genres.isNotEmpty() || filter.tags.isNotEmpty())) {
                     return@withContext searchWebtoons(filter.copy(genres = emptyList(), tags = emptyList()))
                }
                return@withContext emptyList<Webtoon>()
            }

            response.data?.page?.media?.map { media ->
                val contentType = detectContentType(media.format)
                Webtoon(
                    id = media.id.toString(),
                    title = media.title.english ?: media.title.romaji ?: media.title.native ?: "Unknown",
                    description = media.description ?: "",
                    thumbnailUrl = media.coverImage.extraLarge ?: media.coverImage.large,
                    status = media.status ?: "Unknown",
                    genres = media.genres ?: emptyList(),
                    contentType = contentType,
                    readingMode = contentType.readingMode,
                    language = detectLanguageFromFormat(media.format)
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching webtoons", e)
            emptyList()
        }
    }


    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val aniId = id.toIntOrNull() ?: return@withContext Webtoon(id = id, title = "Error: Invalid ID")
            val aniResponse = aniListApi.postQuery(AniListRequest(aniListQuery, mapOf("id" to aniId, "page" to 1, "perPage" to 1)))
            val media = aniResponse.data?.page?.media?.firstOrNull() ?: throw Exception("Media not found in AniList")
            
            val contentType = detectContentType(media.format)
            val language = detectLanguageFromFormat(media.format)

            val titlesToTry = listOfNotNull(
                media.title.english,
                media.title.romaji,
                media.title.native,
                media.title.english?.replace(":", ""),
                media.title.english?.replace("-", " "),
                media.title.english?.substringBefore(":")?.trim(),
                media.title.english?.substringBefore("(")?.trim(),
                media.title.romaji?.replace("-", " "),
                media.title.romaji?.substringBefore(":")?.trim()
            ).distinct()

            var mangaId = ""
            val originalLang = when (media.format) {
                "MANHWA" -> listOf("ko")
                "MANHUA" -> listOf("zh", "zh-hk", "zh-cn")
                else -> null
            }

            for (title in titlesToTry) {
                try {
                    val mdSearch = mangaDexApi.searchManga(title, languages = originalLang)
                    mangaId = mdSearch.data.firstOrNull()?.id ?: ""
                    if (mangaId.isNotEmpty()) break
                } catch (e: Exception) {
                    Log.w(TAG, "Error searching title: $title", e)
                    delay(500)
                }
            }

            val chapters = if (mangaId.isNotEmpty()) {
                val preferredLang = com.example.vantink.data.local.AppPreferences.preferredLanguage.value
                val allChapters = mutableListOf<com.example.vantink.data.remote.dto.MangaDexChapter>()
                var offset = 0
                val limit = 500

                try {
                    do {
                        val feed = mangaDexApi.getMangaFeed(
                            id = mangaId,
                            languages = listOf(preferredLang),
                            order = "asc",
                            limit = limit,
                            offset = offset
                        )
                        allChapters.addAll(feed.data)
                        offset += limit
                    } while (offset < feed.total && feed.data.isNotEmpty())
                } catch (e: Exception) {
                    Log.w(TAG, "Error fetching feed in preferred language, falling back to English", e)
                    if (preferredLang != "en") {
                        try {
                            val enFeed = mangaDexApi.getMangaFeed(mangaId, listOf("en"), "asc", limit, 0)
                            allChapters.addAll(enFeed.data)
                        } catch (e2: Exception) {
                            Log.e(TAG, "Error fetching English feed", e2)
                        }
                    }
                }

                allChapters.map { chapter ->
                    ChapterSummary(
                        id = chapter.id,
                        title = chapter.attributes.title ?: "Chapter ${chapter.attributes.chapter}",
                        number = chapter.attributes.chapter?.toFloatOrNull() ?: 0f,
                        uploadDate = ""
                    )
                }.sortedByDescending { it.number }
            } else {
                Log.w(TAG, "No manga found for ID: $id")
                emptyList()
            }

            Webtoon(
                id = id,
                title = media.title.english ?: media.title.romaji ?: "Unknown",
                description = media.description ?: "",
                thumbnailUrl = media.coverImage.extraLarge ?: media.coverImage.large,
                status = media.status ?: "Unknown",
                genres = media.genres ?: emptyList(),
                chapters = chapters,
                contentType = contentType,
                readingMode = contentType.readingMode,
                language = language
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting webtoon details for ID: $id", e)
            Webtoon(id = id, title = "Error: ${e.localizedMessage?.take(40)}")
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = mangaDexApi.getAtHomeServer(chapterId)
            val hash = response.chapter.hash
            val baseUrl = response.baseUrl
            response.chapter.data.map { filename ->
                "$baseUrl/data/$hash/$filename"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chapter pages for: $chapterId", e)
            emptyList()
        }
    }

    private fun detectContentType(format: String?): ContentType {
        return when (format?.uppercase()) {
            "MANHWA" -> ContentType.MANWHA
            "MANHUA" -> ContentType.MANHUA
            "MANGA" -> ContentType.MANGA
            "ONE_SHOT" -> ContentType.MANGA
            else -> ContentType.UNKNOWN
        }
    }

    private fun detectLanguageFromFormat(format: String?): String {
        return when (format?.uppercase()) {
            "MANHWA" -> "ko"
            "MANHUA" -> "zh-cn"
            "MANGA" -> "ja"
            else -> "en"
        }
    }
}

