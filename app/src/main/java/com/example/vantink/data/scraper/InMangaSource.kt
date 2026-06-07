package com.example.vantink.data.scraper

import android.util.Log
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException

class InMangaSource(
    private val client: OkHttpClient
) : Source {
    override val name: String = "InManga"
    override val baseUrl: String = "https://inmanga.com"

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private val TAG = "InMangaSource"

    override suspend fun searchWebtoons(filter: SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            if (filter.query.isBlank()) return@withContext emptyList()

            val request = Request.Builder()
                .url("$baseUrl/manga/getMangasConsultResult")
                .header("User-Agent", userAgent)
                .header("X-Requested-With", "XMLHttpRequest")
                .post(FormBody.Builder()
                    .add("filter[name]", filter.query)
                    .add("filter[queryString]", filter.query)
                    .build())
                .build()

            val response = client.newCall(request).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.w(TAG, "Search request failed: ${res.code}")
                    return@withContext emptyList()
                }
                res.body?.string() ?: return@withContext emptyList()
            }

            val doc = Jsoup.parse(response)
            
            doc.select("a.manga-result").mapNotNull { element ->
                try {
                    val title = element.selectFirst("h4")?.text()?.trim()
                    val href = element.attr("href")

                    if (title.isNullOrBlank() || href.isBlank()) return@mapNotNull null

                    val id = href.substringAfter("/ver-manga/").takeWhile { it != '/' && it != '?' }
                    if (id.isBlank()) return@mapNotNull null

                    Webtoon(
                        id = id,
                        title = title,
                        thumbnailUrl = "$baseUrl/thumbnails/manga/$id"
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing search result", e)
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error searching webtoons", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching webtoons", e)
            emptyList()
        }
    }

    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/ver-manga/$id"
            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val title = doc.selectFirst("h1")?.text()?.trim() ?: ""
            if (title.isBlank()) {
                Log.w(TAG, "No title found for: $id")
                return@withContext Webtoon(id = id, title = "Unknown")
            }

            val description = doc.selectFirst(".description")?.text()?.trim() ?: ""
            val thumb = "$baseUrl/thumbnails/manga/$id"
            
            // InManga loads chapters via separate request
            val mangaId = doc.selectFirst("#mangaIdentification")?.attr("value")?.trim()
            val chapters = if (!mangaId.isNullOrBlank()) {
                fetchChapters(mangaId)
            } else {
                Log.w(TAG, "No manga ID found for: $id")
                emptyList()
            }

            Webtoon(
                id = id,
                title = title,
                description = description,
                thumbnailUrl = thumb,
                chapters = chapters
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network error getting webtoon details", e)
            Webtoon(id = id, title = "Network error")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading webtoon details for: $id", e)
            Webtoon(id = id, title = "Error: ${e.message?.take(30)}")
        }
    }

    private suspend fun fetchChapters(mangaId: String): List<ChapterSummary> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/chapter/getChapters?mangaIdentification=$mangaId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .build()
            
            val response = client.newCall(request).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.w(TAG, "Chapters request failed: ${res.code}")
                    return@withContext emptyList()
                }
                res.body?.string() ?: return@withContext emptyList()
            }

            try {
                val json = JSONObject(response)
                val result = json.optJSONArray("result")
                if (result == null) {
                    Log.w(TAG, "No result array in chapters response")
                    return@withContext emptyList()
                }

                val list = mutableListOf<ChapterSummary>()
                for (i in 0 until result.length()) {
                    try {
                        val obj = result.getJSONObject(i)
                        val cId = obj.optString("Identification", "")
                        val number = obj.optDouble("Number", 0.0).toFloat()
                        val friendlyTitle = obj.optString("FriendlyTitle", "Chapter $number")

                        if (cId.isNotBlank() && friendlyTitle.isNotBlank()) {
                            list.add(ChapterSummary(id = cId, title = friendlyTitle, number = number))
                        }
                    } catch (e: JSONException) {
                        Log.w(TAG, "Error parsing chapter $i", e)
                    }
                }
                list.sortedByDescending { it.number }
            } catch (e: JSONException) {
                Log.e(TAG, "JSON parsing error in chapters", e)
                emptyList()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching chapters", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chapters for mangaId: $mangaId", e)
            emptyList()
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/chapter/getPages?chapterIdentification=$chapterId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .build()
            
            val response = client.newCall(request).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.w(TAG, "Pages request failed: ${res.code}")
                    return@withContext emptyList()
                }
                res.body?.string() ?: return@withContext emptyList()
            }

            try {
                val json = JSONObject(response)
                val result = json.optJSONArray("result")
                if (result == null) {
                    Log.w(TAG, "No result array in pages response")
                    return@withContext emptyList()
                }

                val pages = mutableListOf<String>()
                for (i in 0 until result.length()) {
                    try {
                        val obj = result.getJSONObject(i)
                        val pageId = obj.optString("Identification", "")
                        if (pageId.isNotBlank()) {
                            pages.add("$baseUrl/images/manga/page/$pageId")
                        }
                    } catch (e: JSONException) {
                        Log.w(TAG, "Error parsing page $i", e)
                    }
                }
                pages
            } catch (e: JSONException) {
                Log.e(TAG, "JSON parsing error in pages", e)
                emptyList()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching pages", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pages for chapter: $chapterId", e)
            emptyList()
        }
    }
}
