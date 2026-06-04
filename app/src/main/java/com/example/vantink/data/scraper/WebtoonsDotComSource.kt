package com.example.vantink.data.scraper

import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class WebtoonsDotComSource : Source {
    override val name: String = "Webtoons.com"
    override val baseUrl: String = "https://www.webtoons.com"

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    override suspend fun searchWebtoons(filter: SearchFilter): List<Webtoon> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/en/search?keyword=${filter.query}"
            val doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .get()

            val results = doc.select(".card_lst li")
            results.map { element ->
                val titleElement = element.selectFirst(".subj")
                val title = titleElement?.text() ?: ""
                val link = element.selectFirst("a")?.attr("href") ?: ""
                val id = extractIdFromUrl(link)
                val thumbnailUrl = element.selectFirst("img")?.attr("src") ?: ""
                val author = element.selectFirst(".author")?.text() ?: ""

                Webtoon(
                    id = id,
                    title = title,
                    author = author,
                    thumbnailUrl = thumbnailUrl
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getWebtoonDetails(id: String): Webtoon = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("$baseUrl/en/search?keyword=$id")
                .userAgent(userAgent)
                .get()
            
            val firstResult = doc.selectFirst(".card_lst li a")
            val detailUrl = firstResult?.attr("href") ?: ""
            
            val detailDoc = Jsoup.connect(detailUrl)
                .userAgent(userAgent)
                .get()
            
            val title = detailDoc.selectFirst(".subj")?.text() ?: ""
            val author = detailDoc.selectFirst(".author")?.text() ?: ""
            val description = detailDoc.selectFirst(".summary")?.text() ?: ""
            val thumb = detailDoc.selectFirst(".detail_header .thmb img")?.attr("src") ?: ""
            
            val chapters = detailDoc.select("#_listUl li").map { element ->
                val cId = element.attr("data-episode-no")
                val cTitle = element.selectFirst(".subj span")?.text() ?: ""
                val cNum = element.selectFirst(".tx")?.text()?.replace("#", "")?.toFloatOrNull() ?: 0f
                val cDate = element.selectFirst(".date")?.text() ?: ""
                
                ChapterSummary(id = cId, title = cTitle, number = cNum, uploadDate = cDate)
            }

            Webtoon(
                id = id,
                title = title,
                author = author,
                description = description,
                thumbnailUrl = thumb,
                chapters = chapters
            )
        } catch (e: Exception) {
            Webtoon(id = id, title = "Error loading details")
        }
    }

    override suspend fun getChapterPages(chapterId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(chapterId)
                .userAgent(userAgent)
                .header("Referer", baseUrl)
                .get()

            doc.select("#_imageList img").map { 
                it.attr("data-url").ifEmpty { it.attr("src") } 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractIdFromUrl(url: String): String {
        return url.substringAfter("title_no=").substringBefore("&")
    }
}
