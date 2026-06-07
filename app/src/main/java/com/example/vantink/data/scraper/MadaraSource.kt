package com.example.vantink.data.scraper

import android.util.Log
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.HttpStatusException
import java.net.SocketTimeoutException

class MadaraSource(
    override val name: String,
    override val baseUrl: String
) : Source {

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private val TAG = "MadaraSource"

    override suspend fun searchWebtoons(filter: SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            val url = if (filter.query.isNotBlank()) {
                "${baseUrl.trimEnd('/')}/?s=${filter.query}&post_type=wp-manga"
            } else {
                "${baseUrl.trimEnd('/')}/manga/?m_orderby=views"
            }
            
            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val results = doc.select(".c-tabs-item__content, .page-item-detail, .manga-item")
            
            results.mapNotNull { element ->
                try {
                    val titleElement = element.selectFirst(".post-title a, .h4 a, .item-summary h3 a")
                    val title = titleElement?.text()?.trim()
                    val detailUrl = titleElement?.absUrl("href")?.trim()

                    if (title.isNullOrBlank() || detailUrl.isNullOrBlank()) {
                        return@mapNotNull null
                    }

                    val thumbnail = element.selectFirst("img")?.let {
                        it.absUrl("data-src").trim()
                            .ifEmpty { it.absUrl("data-lazy-src").trim() }
                            .ifEmpty { it.absUrl("src").trim() }
                    } ?: ""

                    Webtoon(
                        id = detailUrl,
                        title = title,
                        thumbnailUrl = thumbnail
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing webtoon element", e)
                    null
                }
            }
        } catch (e: HttpStatusException) {
            Log.e(TAG, "HTTP Error searching webtoons: ${e.statusCode} - ${e.message}")
            emptyList()
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout searching webtoons", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching webtoons from $baseUrl", e)
            emptyList()
        }
    }

    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(id)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val title = doc.selectFirst(".post-title h1, .post-title h3")?.text()?.trim() ?: ""
            if (title.isBlank()) {
                Log.w(TAG, "No title found for webtoon: $id")
                return@withContext Webtoon(id = id, title = "Unknown")
            }

            val description = doc.selectFirst(".description-summary, .manga-excerpt, .summary__content, .post-content_item p")?.text() ?: ""
            val thumb = doc.selectFirst(".summary_image img, .manga-thumbnail img")?.let {
                it.absUrl("data-src").trim().ifEmpty { it.absUrl("src").trim() }
            } ?: ""
            val author = doc.select(".author-content a, .manga-authors a").text()
            val status = doc.select(".post-status .summary-content, .manga-status").text()
            
            val chapters = doc.select(".wp-manga-chapter, .chapter-link").mapNotNull { element ->
                try {
                    val link = element.selectFirst("a")
                    val cUrl = link?.absUrl("href")?.trim() ?: return@mapNotNull null
                    val cTitle = link.text().trim() ?: element.text().trim()
                    if (cTitle.isBlank()) return@mapNotNull null

                    val cNum = cTitle.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f

                    ChapterSummary(id = cUrl, title = cTitle, number = cNum)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing chapter", e)
                    null
                }
            }.distinctBy { it.id }.sortedByDescending { it.number }

            Webtoon(
                id = id,
                title = title,
                author = author,
                description = description,
                thumbnailUrl = thumb,
                status = status,
                chapters = chapters
            )
        } catch (e: HttpStatusException) {
            Log.e(TAG, "HTTP Error getting webtoon details: ${e.statusCode}")
            Webtoon(id = id, title = "HTTP Error: ${e.statusCode}")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting webtoon details for $id", e)
            Webtoon(id = id, title = "Error: ${e.message?.take(30)}")
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(chapterId)
                .userAgent(userAgent)
                .timeout(10000)
                .get()

            val pages = doc.select(".reading-content img, .wp-manga-chapter-img, .page-break img")
                .mapNotNull { img ->
                    try {
                        val src = img.attr("data-src").trim()
                            .ifEmpty { img.attr("data-lazy-src").trim() }
                            .ifEmpty { img.attr("src").trim() }

                        if (src.isNotEmpty() && !src.contains("adsense", ignoreCase = true)) {
                            src
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                .filter { it.isNotBlank() }

            if (pages.isEmpty()) {
                Log.w(TAG, "No pages found for chapter: $chapterId")
            }
            pages
        } catch (e: HttpStatusException) {
            Log.e(TAG, "HTTP Error getting chapter pages: ${e.statusCode}")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pages for chapter $chapterId", e)
            emptyList()
        }
    }
}
