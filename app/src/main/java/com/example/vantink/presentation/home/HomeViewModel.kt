package com.example.vantink.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val webtoons: List<Webtoon>, val hasMore: Boolean) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repository: WebtoonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private val currentWebtoons = mutableListOf<Webtoon>()

    init {
        loadWebtoons()
    }

    fun loadWebtoons(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (!isLoadMore) {
                _uiState.value = HomeUiState.Loading
                currentPage = 1
                currentWebtoons.clear()
            }
            
            repository.getWebtoons(SearchFilter(sort = "TRENDING_DESC", page = currentPage))
                .onSuccess { webtoons ->
                    currentWebtoons.addAll(webtoons)
                    _uiState.value = HomeUiState.Success(
                        webtoons = currentWebtoons.toList(),
                        hasMore = webtoons.size >= 20
                    )
                    currentPage++
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
