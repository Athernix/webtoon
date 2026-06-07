package com.example.vantink.domain.repository

import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.flow.Flow

interface WebtoonRepository {
    
    // Remote
    suspend fun getWebtoons(filter: com.example.vantink.domain.model.SearchFilter): Result<List<Webtoon>>
    suspend fun searchWebtoons(filter: com.example.vantink.domain.model.SearchFilter): Result<List<Webtoon>>
    suspend fun getWebtoonDetails(id: String): Result<Webtoon>
    suspend fun getChapterDetails(id: String): Result<Chapter>

    // Local - Favorites
    fun getFavorites(): Flow<List<Webtoon>>
    suspend fun addFavorite(webtoon: Webtoon)
    suspend fun removeFavorite(webtoon: Webtoon)
    fun isFavorite(webtoonId: String): Flow<Boolean>

    // Local - History
    fun getHistory(): Flow<List<HistoryEntity>>
    suspend fun updateHistory(history: HistoryEntity)
    suspend fun updateScrollPosition(webtoonId: String, scrollPosition: Int)
    suspend fun getHistoryForWebtoon(webtoonId: String): HistoryEntity?
    suspend fun deleteHistory(webtoonId: String)
    suspend fun clearHistory()

    // Downloads
    fun getAllDownloads(): Flow<List<com.example.vantink.data.local.entity.DownloadEntity>>
    suspend fun startDownload(webtoon: Webtoon, chapter: com.example.vantink.domain.model.ChapterSummary)
    suspend fun deleteDownload(chapterId: String)
    fun getDownloadsForWebtoon(webtoonId: String): Flow<List<com.example.vantink.data.local.entity.DownloadEntity>>

    // Sources
    fun getAllSources(): Flow<List<com.example.vantink.data.local.entity.SourceEntity>>
    suspend fun updateSource(source: com.example.vantink.data.local.entity.SourceEntity)
    suspend fun addSource(source: com.example.vantink.data.local.entity.SourceEntity)
    suspend fun deleteSource(source: com.example.vantink.data.local.entity.SourceEntity)

    // Extension Repositories
    fun getAllRepositories(): Flow<List<com.example.vantink.data.local.entity.RepositoryEntity>>
    suspend fun addRepository(repo: com.example.vantink.data.local.entity.RepositoryEntity)
    suspend fun deleteRepository(repo: com.example.vantink.data.local.entity.RepositoryEntity)
}
