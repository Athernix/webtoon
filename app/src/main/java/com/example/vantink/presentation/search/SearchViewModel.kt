package com.example.vantink.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<Webtoon>, val hasMore: Boolean) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WebtoonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    private var searchJob: Job? = null
    private val currentResults = mutableListOf<Webtoon>()

    fun onQueryChange(newQuery: String) {
        _filter.value = _filter.value.copy(query = newQuery, page = 1)
        triggerSearch(isLoadMore = false)
    }

    fun onFilterChange(newFilter: SearchFilter) {
        _filter.value = newFilter.copy(page = 1)
        triggerSearch(isLoadMore = false)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state is SearchUiState.Success && state.hasMore) {
            _filter.value = _filter.value.copy(page = _filter.value.page + 1)
            triggerSearch(isLoadMore = true)
        }
    }

    private fun triggerSearch(isLoadMore: Boolean) {
        searchJob?.cancel()
        val currentFilter = _filter.value
        
        // Reset results if not loading more
        if (!isLoadMore) {
            currentResults.clear()
            // Immediately show loading to give feedback that filters/query changed
            if (currentFilter.query.isNotBlank() || currentFilter.genres.isNotEmpty() || currentFilter.status != null || currentFilter.tags.isNotEmpty()) {
                _uiState.value = SearchUiState.Loading
            } else {
                _uiState.value = SearchUiState.Idle
                return
            }
        }

        searchJob = viewModelScope.launch {
            if (!isLoadMore) delay(400) // Debounce for typing
            
            repository.searchWebtoons(currentFilter)
                .onSuccess { results ->
                    if (!isLoadMore) currentResults.clear()
                    
                    // Filter duplicates which can happen across sources
                    val newUniqueResults = results.filter { newItem -> 
                        currentResults.none { it.id == newItem.id }
                    }
                    currentResults.addAll(newUniqueResults)
                    
                    _uiState.value = SearchUiState.Success(
                        results = currentResults.toList(),
                        hasMore = results.isNotEmpty() && results.size >= currentFilter.perPage
                    )
                }
                .onFailure { error ->
                    _uiState.value = SearchUiState.Error(error.message ?: "Search failed")
                }
        }
    }
}
