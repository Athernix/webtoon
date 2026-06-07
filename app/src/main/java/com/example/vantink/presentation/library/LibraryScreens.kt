package com.example.vantink.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vantink.data.local.entity.HistoryEntity
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.presentation.components.WebtoonCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onWebtoonClick: (String) -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Library") },
                actions = {
                    IconButton(onClick = { /* Sort */ }) { Icon(Icons.Rounded.Sort, null) }
                    IconButton(onClick = { /* Filter */ }) { Icon(Icons.Rounded.FilterList, null) }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your library is empty", style = MaterialTheme.typography.titleMedium)
                    Text("Add something from Browse", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(favorites, key = { it.id }) { webtoon ->
                    WebtoonCard(
                        webtoon = webtoon,
                        onClick = { onWebtoonClick(webtoon.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onWebtoonClick: (String) -> Unit,
    onChapterClick: (String, String) -> Unit
) {
    val history by viewModel.history.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("History") },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Rounded.Delete, null)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No reading history", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding
            ) {
                items(history, key = { it.webtoonId }) { item ->
                    HistoryItem(
                        item = item,
                        onClick = { onWebtoonClick(item.webtoonId) },
                        onChapterClick = { onChapterClick(item.webtoonId, item.chapterId) },
                        onDelete = { viewModel.deleteHistory(item.webtoonId) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    item: HistoryEntity,
    onClick: () -> Unit,
    onChapterClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1) },
        supportingContent = {
            Column {
                Text("Chapter ${item.chapterNumber}: ${item.chapterTitle}", maxLines = 1)
                Text(
                    text = java.text.DateFormat.getDateTimeInstance().format(item.lastReadDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small).clickable(onClick = onClick),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null)
            }
        },
        modifier = Modifier.clickable(onClick = onChapterClick)
    )
}
