package com.example.vantink.data.repository

import com.example.vantink.data.local.dao.ActiveExtensionDao
import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.data.mapper.toActiveEntity
import com.example.vantink.data.scraper.SourceFactory
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.ExtensionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class ExtensionRepositoryImpl(
    private val client: OkHttpClient,
    private val sourceFactory: SourceFactory,
    private val activeExtensionDao: ActiveExtensionDao
) : ExtensionRepository {

    private var cachedExtensions: List<Extension> = emptyList()

    private companion object {
        const val INDEX_URL = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json"
        const val REPO_BASE_URL = "https://raw.githubusercontent.com/keiyoushi/extensions/repo"
    }

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
                    sourceType = it.sourceType,
                    isMetaProvider = it.isMetaProvider,
                    isDirectory = it.isDirectory,
                    installedVersion = it.version,
                    isActive = true
                )
            }
        }
    }

    override suspend fun getAvailableExtensions(lang: String?): Result<List<Extension>> {
        return runCatching {
            ensureDefaultProviders()
            val activeByPackage = activeExtensionDao.getActiveExtensions().associateBy { it.pkgName }
            val index = if (cachedExtensions.isEmpty()) {
                fetchKeiyoushiIndex().also { cachedExtensions = it }
            } else {
                cachedExtensions
            }

            (defaultProviders() + index)
                .filter { lang == null || it.lang.equals(lang, ignoreCase = true) || it.lang.equals("all", ignoreCase = true) }
                .filter { it.baseUrl.isNotBlank() || it.isMetaProvider }
                .map { extension ->
                    val active = activeByPackage[extension.pkgName]
                    extension.copy(
                        installedVersion = active?.version,
                        isActive = active != null
                    )
                }
                .distinctBy { "${it.pkgName}|${it.baseUrl}" }
                .sortedWith(
                    compareByDescending<Extension> { it.isMetaProvider }
                        .thenByDescending { it.isDirectory }
                        .thenBy { it.lang }
                        .thenBy { it.name.lowercase() }
                )
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
            ensureDefaultProviders()
            coroutineScope {
                activeExtensionDao.getActiveExtensions()
                    .filterNot { it.isDirectory }
                    .take(4)
                    .map { active ->
                        async {
                            runCatching {
                                val sourceEntity = SourceEntity(
                                    id = active.pkgName,
                                    name = active.name,
                                    baseUrl = active.baseUrl,
                                    type = active.sourceType,
                                    iconUrl = active.iconUrl,
                                    lang = active.lang,
                                    version = active.version
                                )
                                sourceFactory.create(sourceEntity)
                                    .searchWebtoons(SearchFilter(query = query))
                                    .map { it.copy(id = "${active.pkgName}|${it.id}", author = active.name) }
                            }.getOrDefault(emptyList())
                        }
                    }
                    .awaitAll()
                    .flatten()
                    .distinctBy { it.id }
            }
        }
    }

    private suspend fun fetchKeiyoushiIndex(): List<Extension> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(INDEX_URL)
            .header("User-Agent", "VantInk Android")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Keiyoushi respondió ${response.code}")
            val body = response.body?.string().orEmpty()
            parseIndex(JSONArray(body))
        }
    }

    private fun parseIndex(array: JSONArray): List<Extension> {
        val extensions = mutableListOf<Extension>()
        for (index in 0 until array.length()) {
            val raw = array.getJSONObject(index)
            val pkgName = raw.optString("pkg", raw.optString("pkgName", ""))
            if (pkgName.isBlank()) continue

            val sources = raw.optJSONArray("sources")
            val apkName = raw.optString("apk", "")
            val iconUrl = if (apkName.isNotBlank()) {
                "$REPO_BASE_URL/icon/${apkName.removeSuffix(".apk")}.png"
            } else {
                ""
            }

            if (sources == null || sources.length() == 0) {
                extensions += raw.toExtension(pkgName, pkgName, raw.optString("baseUrl", ""), iconUrl)
            } else {
                for (sourceIndex in 0 until sources.length()) {
                    val source = sources.optJSONObject(sourceIndex) ?: continue
                    val name = source.optString("name", raw.optString("name", pkgName))
                    val baseUrl = source.optString("baseUrl", raw.optString("baseUrl", ""))
                    val sourceId = source.optString("id", baseUrl.ifBlank { name })
                    val key = if (sources.length() > 1) "$pkgName#$sourceId" else pkgName
                    extensions += raw.toExtension(key, name, baseUrl, iconUrl)
                }
            }
        }
        return extensions
    }

    private fun JSONObject.toExtension(pkgName: String, name: String, baseUrl: String, iconUrl: String): Extension {
        val apkName = optString("apk", "")
        return Extension(
            name = name,
            pkgName = pkgName,
            baseUrl = baseUrl.trimEnd('/'),
            lang = optString("lang", "all"),
            version = optString("version", optInt("code", 0).toString()),
            codeUrl = optString("code_url", optString("codeUrl", "")),
            iconUrl = iconUrl,
            apkUrl = if (apkName.isNotBlank()) "$REPO_BASE_URL/apk/$apkName" else "",
            nsfw = optInt("nsfw", 0) == 1,
            sourceType = inferSourceType(pkgName, name, baseUrl)
        )
    }

    private fun inferSourceType(pkgName: String, name: String, baseUrl: String): String {
        val value = "$pkgName $name $baseUrl".lowercase()
        return when {
            "mangastream" in value || "themesia" in value || "reader" in value -> "mangastream"
            else -> "madara"
        }
    }

    private suspend fun ensureDefaultProviders() {
        val active = activeExtensionDao.getActiveExtensions()
        if (active.none { it.pkgName == META_PROVIDER.pkgName }) {
            activeExtensionDao.upsert(META_PROVIDER.toActiveEntity())
        }
    }

    private fun defaultProviders(): List<Extension> = listOf(META_PROVIDER, EVERYTHING_MOE)

    private val META_PROVIDER = Extension(
        name = "AniList + MangaDex",
        pkgName = "meta.anilist.mangadex",
        baseUrl = "https://mangadex.org",
        lang = "all",
        version = "1.0.0",
        codeUrl = "",
        iconUrl = "",
        apkUrl = "",
        nsfw = false,
        sourceType = "anilist_md",
        isMetaProvider = true,
        isActive = true
    )

    private val EVERYTHING_MOE = Extension(
        name = "EverythingMoe Directory",
        pkgName = "directory.everythingmoe",
        baseUrl = "https://everythingmoe.com",
        lang = "all",
        version = "live",
        codeUrl = "",
        iconUrl = "",
        apkUrl = "",
        nsfw = false,
        sourceType = "directory",
        isDirectory = true
    )
}
