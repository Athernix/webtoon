package com.example.vantink.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.DownloadEntity
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val webtoon: Webtoon) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}

class DetailsViewModel(
    private val webtoonId: String,
    private val repository: WebtoonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(webtoonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val downloads: StateFlow<List<DownloadEntity>> = repository.getDownloadsForWebtoon(webtoonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            repository.getWebtoonDetails(webtoonId)
                .onSuccess { webtoon ->
                    _uiState.value = DetailsUiState.Success(webtoon)
                }
                .onFailure { error ->
                    _uiState.value = DetailsUiState.Error(error.message ?: "Failed to load details")
                }
        }
    }

    fun toggleFavorite(webtoon: Webtoon) {
        viewModelScope.launch {
            if (isFavorite.value) {
                repository.removeFavorite(webtoon)
            } else {
                repository.addFavorite(webtoon)
            }
        }
    }

    fun downloadChapter(chapter: ChapterSummary) {
        val state = uiState.value
        if (state is DetailsUiState.Success) {
            viewModelScope.launch {
                repository.startDownload(state.webtoon, chapter)
            }
        }
    }
}
