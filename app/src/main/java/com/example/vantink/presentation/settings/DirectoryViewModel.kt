package com.example.vantink.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.SourceEntity
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class DirectoryItem(
    val id: String,
    val name: String,
    val link: String,
    val icon: String,
    val type: String
)

class DirectoryViewModel(
    private val repository: WebtoonRepository,
    private val client: OkHttpClient
) : ViewModel() {

    private val _items = MutableStateFlow<List<DirectoryItem>>(emptyList())
    val items: StateFlow<List<DirectoryItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchDirectory()
    }

    fun fetchDirectory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url("https://everythingmoe.com/data/master/prod/manga.json").build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext emptyList<DirectoryItem>()
                        val body = response.body?.string() ?: return@withContext emptyList<DirectoryItem>()
                        val array = JSONArray(body)
                        val list = mutableListOf<DirectoryItem>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(DirectoryItem(
                                id = obj.getString("id"),
                                name = obj.getString("id"), // Usually id is the name in their JSON
                                link = obj.getString("link"),
                                icon = obj.optString("icon", ""),
                                type = obj.optString("type", "manga")
                            ))
                        }
                        list
                    }
                }
                _items.value = results
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAsSource(item: DirectoryItem) {
        viewModelScope.launch {
            // We assume it's Madara if we add it from directory, 
            // though we might need a way to verify or let user choose.
            // For now, let's just add it as a potential source.
            repository.addSource(SourceEntity(
                id = item.link,
                name = item.name,
                baseUrl = item.link,
                type = "madara", // Default assumption for manga sites
                iconUrl = item.icon,
                lang = "en"
            ))
        }
    }
}
