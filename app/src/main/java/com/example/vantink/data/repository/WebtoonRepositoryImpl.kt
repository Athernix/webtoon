package com.example.vantink.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vantink.data.local.dao.DownloadDao
import com.example.vantink.data.local.dao.SourceDao
import com.example.vantink.data.local.dao.WebtoonDao
import com.example.vantink.data.local.entity.DownloadEntity
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.data.mapper.toFavoriteEntity
import com.example.vantink.data.mapper.toWebtoon
import com.example.vantink.data.scraper.DownloadWorker
import com.example.vantink.data.scraper.MadaraSource
import com.example.vantink.data.scraper.MangaStreamSource
import com.example.vantink.data.scraper.Source
import com.example.vantink.data.scraper.SourceFactory
import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WebtoonRepositoryImpl(
    private val context: Context,
    private val primarySource: Source, // Usually AniList+MD
    private val sourceFactory: SourceFactory,
    private val webtoonDao: WebtoonDao,
    private val downloadDao: DownloadDao,
    private val sourceDao: SourceDao,
    private val repositoryDao: com.example.vantink.data.local.dao.RepositoryDao
) : WebtoonRepository {

    override suspend fun getWebtoons(filter: SearchFilter): Result<List<Webtoon>> {
        return try {
            val webtoons = primarySource.searchWebtoons(filter)
            Result.success(webtoons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchWebtoons(filter: SearchFilter): Result<List<Webtoon>> {
        return try {
            val allResults = mutableListOf<Webtoon>()
            
            // 1. Primary Source
            primarySource.searchWebtoons(filter).onEach { allResults.add(it) }
            
            // 2. Secondary Active Sources
            val activeSources = sourceDao.getActiveSources()
            for (sourceEntity in activeSources) {
                try {
                    val source = sourceFactory.create(sourceEntity)
                    val sourceResults = source.searchWebtoons(filter)
                    allResults.addAll(sourceResults.map { it.copy(id = "${sourceEntity.id}|${it.id}") })
                } catch (e: Exception) { /* Log error */ }
            }
            
            Result.success(allResults.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWebtoonDetails(id: String): Result<Webtoon> {
        return try {
            if (id.contains("|")) {
                val (sourceId, realId) = id.split("|", limit = 2)
                // Try to resolve source type from ID
                val source = if (id.contains("mangastream")) MangaStreamSource("", sourceId) else MadaraSource("", sourceId)
                val webtoon = source.getWebtoonDetails(realId)
                Result.success(webtoon.copy(id = id))
            } else {
                val webtoon = primarySource.getWebtoonDetails(id)
                Result.success(webtoon)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChapterDetails(id: String): Result<Chapter> {
        return try {
            if (id.contains("|")) {
                val (sourceId, realId) = id.split("|", limit = 2)
                val source = MadaraSource("", sourceId) 
                val pages = source.getChapterPages(realId)
                Result.success(Chapter(id = id, webtoonId = "", title = "Chapter", number = 0f, pages = pages))
            } else {
                val pages = primarySource.getChapterPages(id)
                Result.success(Chapter(id = id, webtoonId = "", title = "Chapter", number = 0f, pages = pages))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavorites(): Flow<List<Webtoon>> = webtoonDao.getFavorites().map { entities -> entities.map { it.toWebtoon() } }
    override suspend fun addFavorite(webtoon: Webtoon) = webtoonDao.insertFavorite(webtoon.toFavoriteEntity())
    override suspend fun removeFavorite(webtoon: Webtoon) = webtoonDao.deleteFavorite(webtoon.toFavoriteEntity())
    override fun isFavorite(webtoonId: String): Flow<Boolean> = webtoonDao.isFavorite(webtoonId)
    override fun getHistory(): Flow<List<HistoryEntity>> = webtoonDao.getHistory()
    override suspend fun updateHistory(history: HistoryEntity) = webtoonDao.insertHistory(history)
    override suspend fun getHistoryForWebtoon(webtoonId: String): HistoryEntity? = webtoonDao.getHistoryForWebtoon(webtoonId)
    override suspend fun deleteHistory(webtoonId: String) = webtoonDao.deleteHistory(webtoonId)
    override suspend fun clearHistory() = webtoonDao.clearHistory()
    override fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    override fun getDownloadsForWebtoon(webtoonId: String): Flow<List<DownloadEntity>> = downloadDao.getDownloadsForWebtoon(webtoonId)

    override suspend fun startDownload(webtoon: Webtoon, chapter: ChapterSummary) {
        val entity = DownloadEntity(chapterId = chapter.id, webtoonId = webtoon.id, webtoonTitle = webtoon.title, chapterTitle = chapter.title, chapterNumber = chapter.number, status = "PENDING")
        downloadDao.insertDownload(entity)
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>().setInputData(Data.Builder().putString("chapterId", chapter.id).putString("webtoonId", webtoon.id).build()).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override suspend fun deleteDownload(chapterId: String) = downloadDao.deleteDownload(chapterId)

    override fun getAllSources(): Flow<List<SourceEntity>> = sourceDao.getAllSources()
    override suspend fun updateSource(source: SourceEntity) = sourceDao.updateSource(source)
    override suspend fun addSource(source: SourceEntity) = sourceDao.insertSource(source)
    override suspend fun deleteSource(source: SourceEntity) = sourceDao.deleteSource(source)

    override fun getAllRepositories() = repositoryDao.getAllRepositories()
    override suspend fun addRepository(repo: com.example.vantink.data.local.entity.RepositoryEntity) = 
        repositoryDao.insertRepository(repo)
    override suspend fun deleteRepository(repo: com.example.vantink.data.local.entity.RepositoryEntity) = 
        repositoryDao.deleteRepository(repo)
}
