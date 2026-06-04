package com.example.vantink.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: WebtoonRepository
) : ViewModel() {
    val favorites: StateFlow<List<Webtoon>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class HistoryViewModel(
    private val repository: WebtoonRepository
) : ViewModel() {
    val history: StateFlow<List<HistoryEntity>> = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteHistory(webtoonId: String) {
        viewModelScope.launch {
            repository.deleteHistory(webtoonId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
