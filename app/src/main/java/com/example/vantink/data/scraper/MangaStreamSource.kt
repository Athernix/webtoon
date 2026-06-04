package com.example.vantink.data.scraper

import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MangaStreamSource(
    override val name: String,
    override val baseUrl: String
) : Source {

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    override suspend fun searchWebtoons(filter: SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            val url = if (filter.query.isNotBlank()) {
                "$baseUrl/?s=${filter.query}"
            } else {
                "$baseUrl/manga/?order=popular"
            }
            
            val doc = Jsoup.connect(url).userAgent(userAgent).get()
            val results = doc.select(".listupd .bsx, .listupd .utao")
            
            results.map { element ->
                val title = element.selectFirst("a")?.attr("title") ?: element.selectFirst(".subj")?.text() ?: ""
                val detailUrl = element.selectFirst("a")?.attr("href") ?: ""
                val thumbnail = element.selectFirst("img")?.attr("src") ?: ""
                
                Webtoon(
                    id = detailUrl,
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
            val title = doc.selectFirst("h1.entry-title")?.text() ?: ""
            val description = doc.selectFirst(".entry-content p")?.text() ?: ""
            val thumb = doc.selectFirst(".thumb img")?.attr("src") ?: ""
            val author = doc.select(".infotable tr:contains(Author) td:last-child").text()
            val status = doc.select(".infotable tr:contains(Status) td:last-child").text()
            
            val chapters = doc.select("#chapterlist li").map { element ->
                val link = element.selectFirst("a")
                val cUrl = link?.attr("href") ?: ""
                val cTitle = element.select(".chapternum").text().ifEmpty { link?.text() ?: "" }
                val cNum = cTitle.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                
                ChapterSummary(id = cUrl, title = cTitle.trim(), number = cNum)
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
        } catch (e: Exception) {
            Webtoon(id = id, title = "Error loading MangaStream")
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(chapterId).userAgent(userAgent).get()
            // Improved selection for multiple MangaStream variants
            val images = doc.select("#readerarea img, .rdarea img, .reading-content img").map {
                it.attr("src").trim()
                    .ifEmpty { it.attr("data-src").trim() }
                    .ifEmpty { it.attr("data-lazy-src").trim() }
            }.filter { it.isNotEmpty() && !it.contains("adsense", true) && !it.contains("banner", true) }
            
            if (images.isEmpty()) {
                val script = doc.select("script:containsData(ts_reader), script:containsData(sources)").html()
                val regex = """"images":\s*\[([^\]]+)\]""".toRegex()
                val match = regex.find(script)
                match?.groupValues?.get(1)?.split(",")?.map { 
                    it.replace("\"", "").replace("\\/", "/").trim() 
                } ?: emptyList()
            } else {
                images
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
