package com.example.vantink.data.remote

import com.example.vantink.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApiService {
    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 15,
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive"),
        @Query("originalLanguage[]") languages: List<String>? = null
    ): MangaDexListResponse<MangaDexManga>

    @GET("manga/{id}/feed")
    suspend fun getMangaFeed(
        @Path("id") id: String,
        @Query("translatedLanguage[]") languages: List<String>,
        @Query("order[chapter]") order: String = "asc",
        @Query("limit") limit: Int = 500,
        @Query("offset") offset: Int = 0
    ): MangaDexListResponse<MangaDexChapter>

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(
        @Path("chapterId") chapterId: String
    ): MangaDexAtHomeResponse

    companion object {
        const val BASE_URL = "https://api.mangadex.org/"
    }
}
