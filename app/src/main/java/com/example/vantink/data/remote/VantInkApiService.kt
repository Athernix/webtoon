package com.example.vantink.data.remote

import com.example.vantink.data.remote.dto.ChapterDto
import com.example.vantink.data.remote.dto.WebtoonDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VantInkApiService {

    @GET("webtoons")
    suspend fun getWebtoons(
        @Query("page") page: Int = 1,
        @Query("genre") genre: String? = null
    ): List<WebtoonDto>

    @GET("webtoons/search")
    suspend fun searchWebtoons(
        @Query("query") query: String
    ): List<WebtoonDto>

    @GET("webtoons/{id}")
    suspend fun getWebtoonDetails(
        @Path("id") id: String
    ): WebtoonDto

    @GET("chapters/{id}")
    suspend fun getChapterDetails(
        @Path("id") id: String
    ): ChapterDto

    companion object {
        const val BASE_URL = "https://api.vantink.com/"
    }
}
