package com.example.vantink.data.remote.metadata

import com.example.vantink.data.remote.metadata.dto.KitsuMangaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KitsuApiService {
    @GET("manga")
    suspend fun searchManga(
        @Query("filter[text]") query: String,
        @Query("page[limit]") limit: Int = 10
    ): KitsuMangaResponse

    companion object {
        const val BASE_URL = "https://kitsu.io/api/edge/"
    }
}
