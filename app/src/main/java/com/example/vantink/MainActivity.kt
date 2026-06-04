package com.example.vantink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.vantink.presentation.details.DetailsScreen
import com.example.vantink.presentation.details.DetailsViewModel
import com.example.vantink.presentation.home.HomeScreen
import com.example.vantink.presentation.home.HomeViewModel
import com.example.vantink.presentation.library.*
import com.example.vantink.presentation.navigation.Navigator
import com.example.vantink.presentation.navigation.Route
import com.example.vantink.presentation.navigation.rememberNavigationState
import com.example.vantink.presentation.navigation.toEntries
import com.example.vantink.presentation.reader.ReaderScreen
import com.example.vantink.presentation.reader.ReaderViewModel
import com.example.vantink.presentation.search.SearchScreen
import com.example.vantink.presentation.search.SearchViewModel
import com.example.vantink.presentation.settings.DirectoryScreen
import com.example.vantink.presentation.settings.DirectoryViewModel
import com.example.vantink.presentation.settings.SettingsScreen
import com.example.vantink.presentation.settings.SourceScreen
import com.example.vantink.presentation.settings.SourceViewModel
import com.example.vantink.presentation.settings.WebScreen
import com.example.vantink.ui.theme.VantInkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VantInkTheme {
                VantInkAppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun VantInkAppNavigation() {
    val navigationState = rememberNavigationState(
        startRoute = Route.Home,
        topLevelRoutes = setOf(Route.Home, Route.Search, Route.Favorites, Route.History, Route.Downloads)
    )
    val navigator = remember { Navigator(navigationState) }
    val app = (LocalContext.current.applicationContext as VantInkApp)
    val repository = app.repository
    val extensionRepository = app.extensionRepository
    val client = app.okHttpClient

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    val entryProvider = entryProvider<NavKey> {
        entry<Route.Home>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = { com.example.vantink.presentation.components.PlaceholderScreen("Select a webtoon to view details") }
            )
        ) {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(repository)
            }
            HomeScreen(
                viewModel = viewModel,
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onSearchClick = { navigator.navigate(Route.Search) },
                onSettingsClick = { navigator.navigate(Route.Settings) }
            )
        }
        entry<Route.Search>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = { com.example.vantink.presentation.components.PlaceholderScreen("Select a webtoon to view details") }
            )
        ) {
            val viewModel: SearchViewModel = viewModel {
                SearchViewModel(repository)
            }
            SearchScreen(
                viewModel = viewModel,
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Favorites>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = { com.example.vantink.presentation.components.PlaceholderScreen("Select a webtoon to view details") }
            )
        ) {
            val viewModel: FavoritesViewModel = viewModel {
                FavoritesViewModel(repository)
            }
            FavoritesScreen(
                viewModel = viewModel,
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) }
            )
        }
        entry<Route.History>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = { com.example.vantink.presentation.components.PlaceholderScreen("Select a webtoon to view details") }
            )
        ) {
            val viewModel: HistoryViewModel = viewModel {
                HistoryViewModel(repository)
            }
            HistoryScreen(
                viewModel = viewModel,
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onChapterClick = { webtoonId, chapterId ->
                    navigator.navigate(Route.Reader(webtoonId, chapterId))
                }
            )
        }
        entry<Route.Downloads>(
            metadata = ListDetailSceneStrategy.listPane(
                detailPlaceholder = { com.example.vantink.presentation.components.PlaceholderScreen("Select a download to view") }
            )
        ) {
            val viewModel: DownloadViewModel = viewModel {
                DownloadViewModel(repository)
            }
            DownloadScreen(
                viewModel = viewModel,
                onChapterClick = { webtoonId, chapterId ->
                    navigator.navigate(Route.Reader(webtoonId, chapterId))
                },
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Details>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) { key ->
            val viewModel: DetailsViewModel = viewModel(key = key.webtoonId) {
                DetailsViewModel(key.webtoonId, repository)
            }
            DetailsScreen(
                viewModel = viewModel,
                onChapterClick = { webtoonId, chapterId ->
                    navigator.navigate(Route.Reader(webtoonId, chapterId))
                },
                onWebClick = { url, title ->
                    val finalUrl = if (url.startsWith("http")) url else "https://anilist.co/manga/$url"
                    navigator.navigate(Route.Web(finalUrl, title))
                },
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Reader> { key ->
            val viewModel: ReaderViewModel = viewModel(key = key.chapterId) {
                ReaderViewModel(key.webtoonId, key.chapterId, repository)
            }
            ReaderScreen(
                viewModel = viewModel,
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Settings> {
            SettingsScreen(
                onSourcesClick = { navigator.navigate(Route.Sources) },
                onDirectoryClick = { navigator.navigate(Route.Directory) },
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Sources> {
            val viewModel: SourceViewModel = viewModel {
                SourceViewModel(extensionRepository)
            }
            SourceScreen(
                viewModel = viewModel,
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Directory> {
            val viewModel: DirectoryViewModel = viewModel {
                DirectoryViewModel(repository, client)
            }
            DirectoryScreen(
                viewModel = viewModel,
                onBack = { navigator.goBack() }
            )
        }
        entry<Route.Web> { key ->
            WebScreen(
                url = key.url,
                title = key.title,
                onBack = { navigator.goBack() }
            )
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                label = { Text("Home") },
                icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                selected = navigationState.topLevelRoute == Route.Home,
                onClick = { navigator.navigate(Route.Home) }
            )
            item(
                label = { Text("Search") },
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                selected = navigationState.topLevelRoute == Route.Search,
                onClick = { navigator.navigate(Route.Search) }
            )
            item(
                label = { Text("Library") },
                icon = { Icon(Icons.Rounded.Favorite, contentDescription = "Library") },
                selected = navigationState.topLevelRoute == Route.Favorites,
                onClick = { navigator.navigate(Route.Favorites) }
            )
            item(
                label = { Text("Downloads") },
                icon = { Icon(Icons.Rounded.Download, contentDescription = "Downloads") },
                selected = navigationState.topLevelRoute == Route.Downloads,
                onClick = { navigator.navigate(Route.Downloads) }
            )
            item(
                label = { Text("History") },
                icon = { Icon(Icons.Rounded.History, contentDescription = "History") },
                selected = navigationState.topLevelRoute == Route.History,
                onClick = { navigator.navigate(Route.History) }
            )
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                sceneStrategy = listDetailStrategy
            )
        }
    }
}
