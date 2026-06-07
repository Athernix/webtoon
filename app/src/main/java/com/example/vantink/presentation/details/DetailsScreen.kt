package com.example.vantink.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vantink.data.local.entity.DownloadEntity
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.Webtoon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = hiltViewModel(),
    onChapterClick: (String, String) -> Unit,
    onWebClick: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is DetailsUiState.Success) {
                        val webtoon = (uiState as DetailsUiState.Success).webtoon
                        IconButton(
                            onClick = { onWebClick(webtoon.id, webtoon.title) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Rounded.Public, contentDescription = "Web")
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavorite(webtoon) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val state = uiState) {
                is DetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Text(
                            "Tip: Try opening the Web view to bypass protection.",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(onClick = { viewModel.loadDetails() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
                is DetailsUiState.Success -> {
                    DetailsContent(
                        webtoon = state.webtoon,
                        downloads = downloads,
                        onChapterClick = { chapterSummary ->
                            onChapterClick(state.webtoon.id, chapterSummary.id)
                        },
                        onDownloadClick = { viewModel.downloadChapter(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsContent(
    webtoon: Webtoon,
    downloads: List<DownloadEntity>,
    onChapterClick: (ChapterSummary) -> Unit,
    onDownloadClick: (ChapterSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                AsyncImage(
                    model = webtoon.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                startY = 300f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = webtoon.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${webtoon.author} • ${webtoon.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = webtoon.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chapters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(webtoon.chapters) { chapter ->
            val download = downloads.find { it.chapterId == chapter.id }
            ChapterItem(
                chapter = chapter,
                download = download,
                onClick = { onChapterClick(chapter) },
                onDownloadClick = { onDownloadClick(chapter) }
            )
        }
    }
}

@Composable
fun ChapterItem(
    chapter: ChapterSummary,
    download: DownloadEntity?,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(chapter.title) },
        supportingContent = { Text("Chapter ${chapter.number} • ${chapter.uploadDate}") },
        trailingContent = {
            when (download?.status) {
                "COMPLETED" -> Icon(Icons.Rounded.CheckCircle, contentDescription = "Downloaded", tint = Color.Green)
                "DOWNLOADING" -> CircularProgressIndicator(
                    progress = { download.progress / 100f },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                "PENDING" -> Icon(Icons.Rounded.Schedule, contentDescription = "Pending")
                "ERROR" -> IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Rounded.Error, contentDescription = "Error", tint = Color.Red)
                }
                else -> IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Rounded.Download, contentDescription = "Download")
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
