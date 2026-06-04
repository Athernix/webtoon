package com.example.vantink.data.scraper

import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService
import com.example.vantink.data.remote.metadata.dto.AniListRequest
import com.example.vantink.domain.model.ChapterSummary
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
            
            // AniList variables handling
            if (filter.query.isNotBlank()) variables["search"] = filter.query
            
            // AniList expects exactly the enum strings
            if (filter.genres.isNotEmpty()) variables["genres"] = filter.genres
            if (filter.tags.isNotEmpty()) variables["tags"] = filter.tags
            if (filter.status != null) variables["status"] = filter.status
            if (filter.format != null) variables["format"] = filter.format
            
            // Sort must be a list
            variables["sort"] = listOf(filter.sort)
            variables["page"] = filter.page
            variables["perPage"] = filter.perPage
            
            val response = aniListApi.postQuery(AniListRequest(aniListQuery, variables))
            
            if (response.errors != null) {
                // If search has errors (common when mixing tags and search on AniList)
                // Try a fallback search with just the query
                if (filter.query.isNotBlank() && (filter.genres.isNotEmpty() || filter.tags.isNotEmpty())) {
                     return@withContext searchWebtoons(filter.copy(genres = emptyList(), tags = emptyList()))
                }
            }

            response.data?.page?.media?.map { media ->
                Webtoon(
                    id = media.id.toString(),
                    title = media.title.english ?: media.title.romaji ?: media.title.native ?: "Unknown",
                    description = media.description ?: "",
                    thumbnailUrl = media.coverImage.extraLarge ?: media.coverImage.large,
                    status = media.status ?: "Unknown",
                    genres = media.genres ?: emptyList()
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val aniId = id.toIntOrNull() ?: return@withContext Webtoon(id = id, title = "Invalid ID")
            val aniResponse = aniListApi.postQuery(AniListRequest(aniListQuery, mapOf("id" to aniId, "page" to 1, "perPage" to 1)))
            val media = aniResponse.data?.page?.media?.firstOrNull() ?: throw Exception("Not found")
            
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
                "MANHUA" -> listOf("zh", "zh-hk")
                else -> null
            }
            
            for (title in titlesToTry) {
                try {
                    val mdSearch = mangaDexApi.searchManga(title, languages = originalLang)
                    mangaId = mdSearch.data.firstOrNull()?.id ?: ""
                    if (mangaId.isNotEmpty()) break
                } catch (e: Exception) {
                    delay(500)
                }
            }
            
            val chapters = if (mangaId.isNotEmpty()) {
                val preferredLang = com.example.vantink.data.local.AppPreferences.preferredLanguage.value
                val allChapters = mutableListOf<com.example.vantink.data.remote.dto.MangaDexChapter>()
                var offset = 0
                val limit = 500
                
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

                allChapters.map { chapter ->
                    ChapterSummary(
                        id = chapter.id,
                        title = chapter.attributes.title ?: "Chapter ${chapter.attributes.chapter}",
                        number = chapter.attributes.chapter?.toFloatOrNull() ?: 0f,
                        uploadDate = ""
                    )
                }
            } else emptyList()

            Webtoon(
                id = id,
                title = media.title.english ?: media.title.romaji ?: "Unknown",
                description = media.description ?: "",
                thumbnailUrl = media.coverImage.extraLarge ?: media.coverImage.large,
                status = media.status ?: "Unknown",
                genres = media.genres ?: emptyList(),
                chapters = chapters
            )
        } catch (e: Exception) {
            Webtoon(id = id, title = "Error loading details")
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
            emptyList()
        }
    }
}
