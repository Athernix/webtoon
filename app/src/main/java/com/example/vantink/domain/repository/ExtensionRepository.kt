package com.example.vantink.domain.repository

import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.Webtoon
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    fun observeActiveExtensions(): Flow<List<Extension>>
    suspend fun getAvailableExtensions(lang: String? = null): Result<List<Extension>>
    suspend fun activate(extension: Extension)
    suspend fun remove(extension: Extension)
    suspend fun globalSearch(query: String): Result<List<Webtoon>>
}
