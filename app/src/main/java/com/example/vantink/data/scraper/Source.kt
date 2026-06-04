package com.example.vantink.data.scraper

import com.example.vantink.domain.model.Webtoon

interface Source {
    val name: String
    val baseUrl: String

    suspend fun searchWebtoons(filter: com.example.vantink.domain.model.SearchFilter): List<Webtoon>
    suspend fun getWebtoonDetails(id: String): Webtoon
    suspend fun getChapterPages(chapterId: String): List<String>
}
