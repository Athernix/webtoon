package com.example.vantink.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.ReadingMode
import com.example.vantink.domain.model.ContentType
import com.example.vantink.domain.repository.WebtoonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Success(
        val chapter: Chapter,
        val initialScrollPosition: Int,
        val isOffline: Boolean = false,
        val readingMode: ReadingMode = ReadingMode.TOP_TO_BOTTOM,
        val validPages: List<String> = emptyList()
    ) : ReaderUiState
    data class Error(val message: String) : ReaderUiState
}

class ReaderViewModel(
    private val webtoonId: String,
    private val chapterId: String,
    private val repository: WebtoonRepository,
    private val contentType: ContentType = ContentType.UNKNOWN
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadChapter()
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            
            // Check for downloads first
            val downloads = repository.getAllDownloads().first()
            val download = downloads.find { it.chapterId == chapterId && it.status == "COMPLETED" }
            
            if (download != null) {
                val dir = File(download.localPath)
                if (dir.exists()) {
                    val files = dir.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
                    val chapter = Chapter(
                        id = chapterId,
                        webtoonId = webtoonId,
                        title = download.chapterTitle,
                        number = download.chapterNumber,
                        pages = files
                    )
                    _uiState.value = ReaderUiState.Success(
                        chapter,
                        0,
                        isOffline = true,
                        readingMode = contentType.readingMode,
                        validPages = files
                    )
                    return@launch
                }
            }

            val history = repository.getHistoryForWebtoon(webtoonId)
            val initialPos = if (history?.chapterId == chapterId) history.scrollPosition else 0
            val readingMode = history?.readingMode?.let {
                try { ReadingMode.valueOf(it) } catch (e: Exception) { contentType.readingMode }
            } ?: contentType.readingMode

            repository.getChapterDetails(chapterId)
                .onSuccess { chapter ->
                    // Validate URLs
                    val validPages = chapter.pages.filter { it.isValidUrl() }

                    if (validPages.isEmpty()) {
                        _uiState.value = ReaderUiState.Error("No valid pages found in chapter")
                        return@launch
                    }

                    _uiState.value = ReaderUiState.Success(
                        chapter.copy(pages = validPages),
                        initialPos,
                        readingMode = readingMode,
                        validPages = validPages
                    )
                    updateHistory(chapter, initialPos, readingMode)
                }
                .onFailure { error ->
                    _uiState.value = ReaderUiState.Error(error.message ?: "Failed to load chapter")
                }
        }
    }

    fun updateScrollPosition(position: Int) {
        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success) {
            viewModelScope.launch {
                repository.updateScrollPosition(webtoonId, position)
            }
        }
    }

    fun changeReadingMode(newMode: ReadingMode) {
        val currentState = _uiState.value
        if (currentState is ReaderUiState.Success) {
            _uiState.value = currentState.copy(readingMode = newMode)
        }
    }

    private suspend fun updateHistory(chapter: Chapter, position: Int, readingMode: ReadingMode) {
        val existingHistory = repository.getHistoryForWebtoon(webtoonId)
        val history = HistoryEntity(
            webtoonId = webtoonId,
            title = existingHistory?.title ?: "Webtoon",
            thumbnailUrl = existingHistory?.thumbnailUrl ?: "",
            chapterId = chapter.id,
            chapterTitle = chapter.title,
            chapterNumber = chapter.number,
            scrollPosition = position,
            contentType = existingHistory?.contentType ?: contentType.name,
            readingMode = readingMode.name,
            language = existingHistory?.language ?: "en",
            lastReadDate = System.currentTimeMillis()
        )
        repository.updateHistory(history)
    }
}

private fun String.isValidUrl(): Boolean {
    return try {
        URL(this)
        this.startsWith("http://") || this.startsWith("https://")
    } catch (e: Exception) {
        false
    }
}

