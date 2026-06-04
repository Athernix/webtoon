package com.example.vantink.data.remote.metadata

import com.example.vantink.data.remote.metadata.dto.AniListRequest
import com.example.vantink.data.remote.metadata.dto.AniListResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AniListApiService {
    @POST("/")
    suspend fun postQuery(@Body request: AniListRequest): AniListResponse

    companion object {
        const val BASE_URL = "https://graphql.anilist.co"
    }
}
