package com.example.vantink.data.scraper

import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceFactory @Inject constructor(
    private val aniListApi: AniListApiService,
    private val mangaDexApi: MangaDexApiService,
    private val client: okhttp3.OkHttpClient
) {
    fun create(entity: SourceEntity): Source {
        return when (entity.type) {
            "madara" -> MadaraSource(entity.name, entity.baseUrl)
            "mangastream" -> MangaStreamSource(entity.name, entity.baseUrl)
            "inmanga" -> InMangaSource(client)
            "anilist_md" -> AniListMangaDexSource(aniListApi, mangaDexApi)
            else -> AniListMangaDexSource(aniListApi, mangaDexApi) // Default
        }
    }
}
