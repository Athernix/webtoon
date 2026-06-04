package com.example.vantink.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.DownloadEntity
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadViewModel(
    private val repository: WebtoonRepository
) : ViewModel() {
    val downloads: StateFlow<List<DownloadEntity>> = repository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDownload(chapterId: String) {
        viewModelScope.launch {
            repository.deleteDownload(chapterId)
        }
    }
}
