package com.example.vantink.data.repository

import com.example.vantink.data.local.dao.ActiveExtensionDao
import com.example.vantink.data.mapper.toActiveEntity
import com.example.vantink.data.mapper.toDomain
import com.example.vantink.data.mapper.toWebtoon
import com.example.vantink.data.remote.KeiyoushiApiService
import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.ExtensionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExtensionRepositoryImpl(
    private val api: KeiyoushiApiService,
    private val activeExtensionDao: ActiveExtensionDao
) : ExtensionRepository {

    override fun observeActiveExtensions(): Flow<List<Extension>> {
        return activeExtensionDao.observeActiveExtensions().map { active ->
            active.map {
                Extension(
                    name = it.name,
                    pkgName = it.pkgName,
                    baseUrl = it.baseUrl,
                    lang = it.lang,
                    version = it.version,
                    codeUrl = "",
                    iconUrl = it.iconUrl,
                    apkUrl = "",
                    nsfw = false,
                    installedVersion = it.version,
                    isActive = true
                )
            }
        }
    }

    override suspend fun getAvailableExtensions(lang: String?): Result<List<Extension>> {
        return runCatching {
            val activeByPackage = activeExtensionDao.getActiveExtensions().associateBy { it.pkgName }
            api.getAvailableExtensions(lang)
                .map { it.toDomain(activeByPackage[it.pkgName]) }
                .distinctBy { it.pkgName }
                .sortedWith(compareBy<Extension> { it.lang }.thenBy { it.name.lowercase() })
        }
    }

    override suspend fun activate(extension: Extension) {
        activeExtensionDao.upsert(extension.toActiveEntity())
    }

    override suspend fun remove(extension: Extension) {
        activeExtensionDao.deleteByPackage(extension.pkgName)
    }

    override suspend fun globalSearch(query: String): Result<List<Webtoon>> {
        return runCatching {
            coroutineScope {
                activeExtensionDao.getActiveExtensions()
                    .take(4)
                    .map { active ->
                        async {
                            runCatching {
                                api.searchSource(active.pkgName, query).map { it.toWebtoon() }
                            }.getOrDefault(emptyList())
                        }
                    }
                    .awaitAll()
                    .flatten()
                    .distinctBy { it.id }
            }
        }
    }
}
