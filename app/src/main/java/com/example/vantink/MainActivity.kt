package com.example.vantink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import com.example.vantink.presentation.settings.*
import com.example.vantink.ui.theme.VantInkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        startRoute = Route.Library,
        topLevelRoutes = setOf(Route.Library, Route.Updates, Route.History, Route.Browse, Route.More)
    )
    val navigator = remember { Navigator(navigationState) }

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    val entryProvider = entryProvider<NavKey> {
        entry<Route.Library>(metadata = ListDetailSceneStrategy.listPane()) {
            FavoritesScreen(
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) }
            )
        }
        
        entry<Route.Updates>(metadata = ListDetailSceneStrategy.listPane()) {
            PlaceholderScreen("New chapter updates will appear here")
        }

        entry<Route.History>(metadata = ListDetailSceneStrategy.listPane()) {
            HistoryScreen(
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onChapterClick = { webtoonId, chapterId -> navigator.navigate(Route.Reader(webtoonId, chapterId)) }
            )
        }

        entry<Route.Browse>(metadata = ListDetailSceneStrategy.listPane()) {
            HomeScreen(
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onSearchClick = { navigator.navigate(Route.Search) },
                onSettingsClick = { navigator.navigate(Route.Extensions) }
            )
        }

        entry<Route.More>(metadata = ListDetailSceneStrategy.listPane()) {
            SettingsScreen(
                onSourcesClick = { navigator.navigate(Route.Extensions) },
                onDirectoryClick = { navigator.navigate(Route.Directory) },
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Search> {
            SearchScreen(
                onWebtoonClick = { id -> navigator.navigate(Route.Details(id)) },
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Extensions> {
            SourceScreen(
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Directory> {
            DirectoryScreen(
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Details>(metadata = ListDetailSceneStrategy.detailPane()) { 
            DetailsScreen(
                onChapterClick = { webtoonId, chapterId -> navigator.navigate(Route.Reader(webtoonId, chapterId)) },
                onWebClick = { id, title ->
                    val realId = if (id.contains("|")) id.substringAfter("|") else id
                    val finalUrl = if (realId.startsWith("http")) realId else "https://anilist.co/manga/$realId"
                    navigator.navigate(Route.Web(finalUrl, title))
                },
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Reader> { 
            ReaderScreen(
                onBack = { navigator.goBack() }
            )
        }

        entry<Route.Web> { key ->
            WebScreen(url = key.url, title = key.title, onBack = { navigator.goBack() })
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                label = { Text("Library") },
                icon = { Icon(Icons.Rounded.CollectionsBookmark, null) },
                selected = navigationState.topLevelRoute == Route.Library,
                onClick = { navigator.navigate(Route.Library) }
            )
            item(
                label = { Text("Updates") },
                icon = { Icon(Icons.Rounded.Update, null) },
                selected = navigationState.topLevelRoute == Route.Updates,
                onClick = { navigator.navigate(Route.Updates) }
            )
            item(
                label = { Text("History") },
                icon = { Icon(Icons.Rounded.History, null) },
                selected = navigationState.topLevelRoute == Route.History,
                onClick = { navigator.navigate(Route.History) }
            )
            item(
                label = { Text("Browse") },
                icon = { Icon(Icons.Rounded.Explore, null) },
                selected = navigationState.topLevelRoute == Route.Browse,
                onClick = { navigator.navigate(Route.Browse) }
            )
            item(
                label = { Text("More") },
                icon = { Icon(Icons.Rounded.MoreHoriz, null) },
                selected = navigationState.topLevelRoute == Route.More,
                onClick = { navigator.navigate(Route.More) }
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

@Composable
fun PlaceholderScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
