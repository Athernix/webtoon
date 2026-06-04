package com.example.vantink.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel,
    onChapterClick: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val downloads by viewModel.downloads.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (downloads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No downloads yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding
            ) {
                items(downloads, key = { it.chapterId }) { item ->
                    ListItem(
                        headlineContent = { Text(item.webtoonTitle, fontWeight = FontWeight.Bold) },
                        supportingContent = { 
                            Column {
                                Text("${item.chapterTitle} (${item.status})")
                                if (item.status == "DOWNLOADING") {
                                    LinearProgressIndicator(
                                        progress = { item.progress / 100f },
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteDownload(item.chapterId) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove")
                            }
                        },
                        modifier = Modifier.clickable { 
                            if (item.status == "COMPLETED") {
                                onChapterClick(item.webtoonId, item.chapterId)
                            }
                        }
                    )
                }
            }
        }
    }
}
