package com.example.vantink.data.remote.metadata

import com.example.vantink.data.remote.metadata.dto.JikanMangaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JikanApiService {
    @GET("top/manga")
    suspend fun getTopManga(
        @Query("type") type: String = "manhwa",
        @Query("page") page: Int = 1
    ): JikanMangaResponse

    companion object {
        const val BASE_URL = "https://api.jikan.moe/v4/"
    }
}
