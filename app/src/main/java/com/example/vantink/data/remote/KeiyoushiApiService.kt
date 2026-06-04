package com.example.vantink.data.remote

import com.example.vantink.data.remote.dto.ExtensionChapterPagesDto
import com.example.vantink.data.remote.dto.ExtensionComicDto
import com.example.vantink.data.remote.dto.ExtensionDto
import com.example.vantink.data.remote.dto.ExtensionSearchResultDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KeiyoushiApiService {
    @GET("extensions/available")
    suspend fun getAvailableExtensions(
        @Query("lang") lang: String? = null
    ): List<ExtensionDto>

    @GET("source/{pkgName}/search")
    suspend fun searchSource(
        @Path("pkgName") pkgName: String,
        @Query("q") query: String
    ): List<ExtensionSearchResultDto>

    @GET("source/{pkgName}/comic/{id}")
    suspend fun getComic(
        @Path("pkgName") pkgName: String,
        @Path(value = "id", encoded = true) id: String
    ): ExtensionComicDto

    @GET("source/{pkgName}/chapter/{id}")
    suspend fun getChapterPages(
        @Path("pkgName") pkgName: String,
        @Path(value = "id", encoded = true) id: String
    ): ExtensionChapterPagesDto

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
    }
}
