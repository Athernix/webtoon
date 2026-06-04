package com.example.vantink.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.RepositoryEntity
import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class SourceViewModel(
    private val repository: WebtoonRepository,
    private val client: OkHttpClient
) : ViewModel() {

    val installedSources: StateFlow<List<SourceEntity>> = repository.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repositories: StateFlow<List<RepositoryEntity>> = repository.getAllRepositories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _discoveryResults = MutableStateFlow<List<SourceEntity>>(emptyList())
    val discoveryResults: StateFlow<List<SourceEntity>> = _discoveryResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun addRepository(url: String) {
        viewModelScope.launch {
            repository.addRepository(RepositoryEntity(url, "Extension Repo"))
        }
    }

    fun removeRepository(repo: RepositoryEntity) {
        viewModelScope.launch {
            repository.deleteRepository(repo)
        }
    }

    fun fetchFromRepository(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Handle different index filenames
                val urls = if (url.endsWith(".json")) listOf(url) 
                          else listOf("$url/index.json", "$url/anime_index.json", "$url/novel_index.json")
                
                val allSources = mutableListOf<SourceEntity>()
                
                withContext(Dispatchers.IO) {
                    for (targetUrl in urls) {
                        try {
                            val request = Request.Builder().url(targetUrl).build()
                            client.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) return@use
                                val body = response.body?.string() ?: return@use
                                val array = JSONArray(body)
                                for (i in 0 until array.length()) {
                                    val obj = array.getJSONObject(i)
                                    if (obj.optString("typeSource") == "madara") {
                                        allSources.add(SourceEntity(
                                            id = obj.getString("baseUrl"),
                                            name = obj.getString("name"),
                                            baseUrl = obj.getString("baseUrl"),
                                            type = "madara",
                                            iconUrl = obj.optString("iconUrl", ""),
                                            lang = obj.getString("lang")
                                        ))
                                    }
                                }
                            }
                        } catch (e: Exception) { /* Skip invalid index */ }
                    }
                }
                _discoveryResults.value = allSources.distinctBy { it.id }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun installSource(source: SourceEntity) {
        viewModelScope.launch {
            repository.addSource(source)
        }
    }

    fun toggleSource(source: SourceEntity) {
        viewModelScope.launch {
            repository.updateSource(source.copy(isEnabled = !source.isEnabled))
        }
    }

    fun uninstallSource(source: SourceEntity) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }
}
