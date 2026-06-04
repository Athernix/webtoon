package com.example.vantink.data.scraper

import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService

class SourceFactory(
    private val aniListApi: AniListApiService,
    private val mangaDexApi: MangaDexApiService
) {
    fun create(entity: SourceEntity): Source {
        return when (entity.type) {
            "madara" -> MadaraSource(entity.name, entity.baseUrl)
            "anilist_md" -> AniListMangaDexSource(aniListApi, mangaDexApi)
            else -> AniListMangaDexSource(aniListApi, mangaDexApi) // Default
        }
    }
}
