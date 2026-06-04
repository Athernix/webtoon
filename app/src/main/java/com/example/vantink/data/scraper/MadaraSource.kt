package com.example.vantink.data.scraper

import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MadaraSource(
    override val name: String,
    override val baseUrl: String
) : Source {

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    override suspend fun searchWebtoons(filter: SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            val url = if (filter.query.isNotBlank()) {
                "$baseUrl/?s=${filter.query}&post_type=wp-manga"
            } else {
                "$baseUrl/manga/?m_orderby=views"
            }
            
            val doc = Jsoup.connect(url).userAgent(userAgent).get()
            val results = doc.select(".c-tabs-item__content, .page-item-detail")
            
            results.map { element ->
                val titleElement = element.selectFirst(".post-title a, .h4 a")
                val title = titleElement?.text() ?: ""
                val detailUrl = titleElement?.attr("href") ?: ""
                val thumbnail = element.selectFirst("img")?.let { 
                    it.attr("data-src").ifEmpty { it.attr("src") }
                } ?: ""
                
                Webtoon(
                    id = detailUrl, // For Madara, we'll use the full URL as ID
                    title = title,
                    thumbnailUrl = thumbnail
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(id).userAgent(userAgent).get()
            val title = doc.selectFirst(".post-title h1")?.text() ?: ""
            val description = doc.selectFirst(".description-summary, .manga-excerpt, .summary__content")?.text() ?: ""
            val thumb = doc.selectFirst(".summary_image img, .manga-thumbnail img")?.let {
                it.attr("data-src").ifEmpty { it.attr("src") }
            } ?: ""
            val author = doc.select(".author-content a, .manga-authors a").text()
            val status = doc.select(".post-status .summary-content, .manga-status").text()
            
            // Madara often loads chapters via AJAX or a specific list
            val chapters = doc.select(".wp-manga-chapter, .chapter-link").map { element ->
                val link = element.selectFirst("a")
                val cUrl = link?.attr("href") ?: ""
                val cTitle = link?.text() ?: element.text()
                val cNum = cTitle.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                
                ChapterSummary(id = cUrl, title = cTitle.trim(), number = cNum)
            }.sortedByDescending { it.number }.distinctBy { it.id }

            Webtoon(
                id = id,
                title = title,
                author = author,
                description = description,
                thumbnailUrl = thumb,
                status = status,
                chapters = chapters
            )
        } catch (e: Exception) {
            Webtoon(id = id, title = "Error loading Madara")
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(chapterId).userAgent(userAgent).get()
            doc.select(".reading-content img, .wp-manga-chapter-img, .page-break img").map {
                val url = it.attr("data-src").trim()
                    .ifEmpty { it.attr("data-lazy-src").trim() }
                    .ifEmpty { it.attr("src").trim() }
                url
            }.filter { it.isNotEmpty() && !it.contains("adsense", true) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
