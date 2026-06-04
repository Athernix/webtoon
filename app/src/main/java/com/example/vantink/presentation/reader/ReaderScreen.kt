package com.example.vantink.presentation.reader

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.example.vantink.domain.model.Chapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    DisposableEffect(Unit) {
        window?.let {
            val controller = WindowCompat.getInsetsController(it, it.decorView)
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            window?.let {
                val controller = WindowCompat.getInsetsController(it, it.decorView)
                controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            var visible by remember { mutableStateOf(true) }
            // Toggling top bar could be added via click listener on the list
            if (visible) {
                TopAppBar(
                    title = {
                        if (uiState is ReaderUiState.Success) {
                            Text((uiState as ReaderUiState.Success).chapter.title)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ReaderUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReaderUiState.Error -> {
                    Text(
                        state.message,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ReaderUiState.Success -> {
                    if (state.chapter.pages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("This chapter has no pages.", color = Color.White)
                        }
                    } else {
                        ChapterImageList(
                            chapter = state.chapter,
                            initialIndex = state.initialScrollPosition,
                            onScroll = viewModel::updateScrollPosition
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterImageList(
    chapter: Chapter,
    initialIndex: Int,
    onScroll: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Sync scroll position back to ViewModel
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onScroll(listState.firstVisibleItemIndex)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(chapter.pages) { index, imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Page ${index + 1}",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}
