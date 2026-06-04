package com.example.vantink.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.ExtensionRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ExtensionUiState {
    data object Loading : ExtensionUiState
    data class Success(val list: List<Extension>) : ExtensionUiState
    data class Error(val message: String) : ExtensionUiState
}

sealed interface ExploreUiState {
    data object Idle : ExploreUiState
    data object Loading : ExploreUiState
    data class Success(val list: List<Webtoon>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}

@OptIn(FlowPreview::class)
class SourceViewModel(
    private val repository: ExtensionRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedLang = MutableStateFlow("es")
    val selectedLang: StateFlow<String> = _selectedLang.asStateFlow()

    private val _uiState = MutableStateFlow<ExtensionUiState>(ExtensionUiState.Loading)
    val uiState: StateFlow<ExtensionUiState> = _uiState.asStateFlow()

    val activeExtensions: StateFlow<List<Extension>> = repository.observeActiveExtensions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _exploreQuery = MutableStateFlow("")
    val exploreQuery: StateFlow<String> = _exploreQuery.asStateFlow()

    private val _exploreState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val exploreState: StateFlow<ExploreUiState> = _exploreState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            combine(_query.debounce(180), _selectedLang) { _, _ -> Unit }
                .collect { refresh() }
        }
    }

    fun updateQuery(value: String) {
        _query.value = value
    }

    fun selectLanguage(lang: String) {
        _selectedLang.value = lang
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = ExtensionUiState.Loading
            repository.getAvailableExtensions(_selectedLang.value.takeUnless { it == "all" })
                .onSuccess { list ->
                    val filtered = list.filter { extension ->
                        _query.value.isBlank() || extension.name.contains(_query.value, ignoreCase = true) ||
                            extension.pkgName.contains(_query.value, ignoreCase = true)
                    }
                    _uiState.value = ExtensionUiState.Success(filtered)
                }
                .onFailure {
                    _uiState.value = ExtensionUiState.Error(it.message ?: "No se pudo cargar Keiyoushi")
                }
        }
    }

    fun activate(extension: Extension) {
        viewModelScope.launch {
            repository.activate(extension)
            refresh()
        }
    }

    fun remove(extension: Extension) {
        viewModelScope.launch {
            repository.remove(extension)
            refresh()
        }
    }

    fun updateExploreQuery(value: String) {
        _exploreQuery.value = value
    }

    fun globalSearch() {
        val text = _exploreQuery.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _exploreState.value = ExploreUiState.Loading
            repository.globalSearch(text)
                .onSuccess { _exploreState.value = ExploreUiState.Success(it) }
                .onFailure { _exploreState.value = ExploreUiState.Error(it.message ?: "Sin resultados") }
        }
    }
}
