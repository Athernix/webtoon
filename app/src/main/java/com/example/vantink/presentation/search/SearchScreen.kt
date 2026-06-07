package com.example.vantink.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.presentation.components.WebtoonCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onWebtoonClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    // Pagination trigger
    LaunchedEffect(gridState.canScrollForward) {
        if (!gridState.canScrollForward && uiState is SearchUiState.Success) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = filter.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search webtoons...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                if (filter.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                IconButton(onClick = { showFilters = true }) {
                                    Icon(
                                        Icons.Rounded.FilterList, 
                                        contentDescription = "Filters",
                                        tint = if (filter.genres.isNotEmpty() || filter.status != null || filter.tags.isNotEmpty()) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                    )
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Text(
                        "Start typing or apply filters",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(
                            "No results found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Adaptive(140.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.results, key = { it.id }) { webtoon ->
                                WebtoonCard(
                                    webtoon = webtoon,
                                    onClick = { onWebtoonClick(webtoon.id) }
                                )
                            }
                            
                            if (state.hasMore) {
                                item {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            currentFilter = filter,
            onFilterChange = viewModel::onFilterChange,
            onDismiss = { showFilters = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val genres = listOf("Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", "Horror", "Mahou Shoujo", "Mecha", "Music", "Mystery", "Psychological", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller")
    val tags = listOf("Vampire", "System", "Reincarnation", "Villainess", "Isekai", "Magic", "Zombies", "Post-Apocalyptic", "Historical", "Martial Arts", "Cultivation", "Monster Girls", "Office Workers", "Super Power", "Aliens", "Survival", "Time Skip", "Gore", "Demons", "Ghosts", "School", "Military", "Cyberpunk", "Steam Punk")
    val statuses = mapOf("FINISHED" to "Finished", "RELEASING" to "Releasing", "CANCELLED" to "Cancelled", "HIATUS" to "Hiatus")
    val formats = mapOf("MANGA" to "Manga", "MANHWA" to "Manhwa", "MANHUA" to "Manhua", "NOVEL" to "Novel", "ONE_SHOT" to "One Shot")
    val sorts = mapOf("TRENDING_DESC" to "Trending", "POPULARITY_DESC" to "Popularity", "SCORE_DESC" to "Score", "UPDATED_AT_DESC" to "Last Updated")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Sort By", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sorts.forEach { (key, label) ->
                    FilterChip(
                        selected = currentFilter.sort == key,
                        onClick = { onFilterChange(currentFilter.copy(sort = key)) },
                        label = { Text(label) }
                    )
                }
            }

            Text("Format", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter.format == null,
                    onClick = { onFilterChange(currentFilter.copy(format = null)) },
                    label = { Text("All") }
                )
                formats.forEach { (key, label) ->
                    FilterChip(
                        selected = currentFilter.format == key,
                        onClick = { onFilterChange(currentFilter.copy(format = key)) },
                        label = { Text(label) }
                    )
                }
            }

            Text("Status", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter.status == null,
                    onClick = { onFilterChange(currentFilter.copy(status = null)) },
                    label = { Text("All") }
                )
                statuses.forEach { (key, label) ->
                    FilterChip(
                        selected = currentFilter.status == key,
                        onClick = { onFilterChange(currentFilter.copy(status = key)) },
                        label = { Text(label) }
                    )
                }
            }

            Text("Genres", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                genres.forEach { genre ->
                    val isSelected = currentFilter.genres.contains(genre)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newGenres = if (isSelected) currentFilter.genres - genre else currentFilter.genres + genre
                            onFilterChange(currentFilter.copy(genres = newGenres))
                        },
                        label = { Text(genre) }
                    )
                }
            }

            Text("Tags", style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    val isSelected = currentFilter.tags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTags = if (isSelected) currentFilter.tags - tag else currentFilter.tags + tag
                            onFilterChange(currentFilter.copy(tags = newTags))
                        },
                        label = { Text(tag) }
                    )
                }
            }
            
            Button(
                onClick = {
                    onFilterChange(SearchFilter(query = currentFilter.query))
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Reset Filters")
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Apply Filters")
            }
        }
    }
}
