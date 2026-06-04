package com.example.vantink.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vantink.data.local.dao.ActiveExtensionDao
import com.example.vantink.data.local.dao.DownloadDao
import com.example.vantink.data.local.dao.SourceDao
import com.example.vantink.data.local.dao.WebtoonDao
import com.example.vantink.data.local.entity.ActiveExtensionEntity
import com.example.vantink.data.local.entity.DownloadEntity
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.data.mapper.toFavoriteEntity
import com.example.vantink.data.mapper.toWebtoon
import com.example.vantink.data.scraper.DownloadWorker
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
    private val repositoryDao: com.example.vantink.data.local.dao.RepositoryDao,
    private val activeExtensionDao: ActiveExtensionDao
) : WebtoonRepository {

    override suspend fun getWebtoons(filter: SearchFilter): Result<List<Webtoon>> {
        return try {
            val activeSources = readableSources()

            val webtoons = activeSources.flatMap { extension ->
                runCatching {
                    sourceFactory.create(extension.toSourceEntity())
                        .searchWebtoons(filter.copy(query = ""))
                        .map { it.withSource(extension) }
                }.getOrDefault(emptyList())
            }
            Result.success(webtoons.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchWebtoons(filter: SearchFilter): Result<List<Webtoon>> {
        return try {
            val activeSources = readableSources()
            if (activeSources.isEmpty()) {
                return Result.failure(IllegalStateException("No hay extensiones activas. Instala una fuente desde Tienda de Fuentes."))
            }

            val allResults = activeSources.flatMap { extension ->
                runCatching {
                    sourceFactory.create(extension.toSourceEntity())
                        .searchWebtoons(filter)
                        .map { it.withSource(extension) }
                }.getOrDefault(emptyList())
            }

            Result.success(allResults.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWebtoonDetails(id: String): Result<Webtoon> {
        return try {
            val sourceRef = SourceRef.from(id)
            if (sourceRef != null) {
                val extension = activeExtensionDao.getActiveExtensions()
                    .firstOrNull { it.pkgName == sourceRef.pkgName }
                    ?: return Result.failure(IllegalStateException("La extension ${sourceRef.pkgName} ya no esta activa."))

                val source = sourceFactory.create(extension.toSourceEntity())
                val webtoon = source.getWebtoonDetails(sourceRef.realId)
                Result.success(
                    webtoon.withSource(extension, sourceRef.realId)
                        .copy(chapters = webtoon.chapters.map { chapter ->
                            chapter.copy(id = "${extension.pkgName}|${chapter.id}")
                        })
                )
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
            val sourceRef = SourceRef.from(id)
            if (sourceRef != null) {
                val extension = activeExtensionDao.getActiveExtensions()
                    .firstOrNull { it.pkgName == sourceRef.pkgName }
                    ?: return Result.failure(IllegalStateException("La extension ${sourceRef.pkgName} ya no esta activa."))

                val pages = sourceFactory.create(extension.toSourceEntity()).getChapterPages(sourceRef.realId)
                Result.success(Chapter(id = id, webtoonId = sourceRef.pkgName, title = "Chapter", number = 0f, pages = pages))
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

    private fun ActiveExtensionEntity.toSourceEntity(): SourceEntity {
        return SourceEntity(
            id = pkgName,
            name = name,
            baseUrl = baseUrl,
            type = sourceType,
            iconUrl = iconUrl,
            isEnabled = true,
            lang = lang,
            version = version,
            repoUrl = "keiyoushi"
        )
    }

    private suspend fun readableSources(): List<ActiveExtensionEntity> {
        val activeSources = activeExtensionDao.getActiveExtensions()
        if (activeSources.none { it.pkgName == DEFAULT_META_PROVIDER.pkgName }) {
            activeExtensionDao.upsert(DEFAULT_META_PROVIDER)
        }
        return activeExtensionDao.getActiveExtensions()
            .filterNot { it.isDirectory }
            .ifEmpty { listOf(DEFAULT_META_PROVIDER) }
    }

    private fun Webtoon.withSource(extension: ActiveExtensionEntity, realId: String = id): Webtoon {
        return copy(
            id = "${extension.pkgName}|$realId",
            author = author.ifBlank { extension.name },
            genres = (genres + extension.lang.uppercase()).distinct()
        )
    }

    private data class SourceRef(val pkgName: String, val realId: String) {
        companion object {
            fun from(id: String): SourceRef? {
                val separator = id.indexOf('|')
                if (separator <= 0 || separator == id.lastIndex) return null
                return SourceRef(id.substring(0, separator), id.substring(separator + 1))
            }
        }
    }

    private companion object {
        val DEFAULT_META_PROVIDER = ActiveExtensionEntity(
            pkgName = "meta.anilist.mangadex",
            name = "AniList + MangaDex",
            baseUrl = "https://mangadex.org",
            lang = "all",
            version = "1.0.0",
            sourceType = "anilist_md",
            isMetaProvider = true
        )
    }
}
