package com.example.vantink.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vantink.data.local.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSourcesClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by AppPreferences.preferredLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Content",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ListItem(
                headlineContent = { Text("Extensions & Sources") },
                supportingContent = { Text("Manage installed scrapers and repositories") },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onSourcesClick)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Reading Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ListItem(
                headlineContent = { Text("Chapter Language") },
                supportingContent = { Text("Currently: ${if (currentLang == "es") "Español" else "English"}") },
                trailingContent = {
                    Row {
                        FilterChip(
                            selected = currentLang == "es",
                            onClick = { AppPreferences.setLanguage(context, "es") },
                            label = { Text("ES") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = currentLang == "en",
                            onClick = { AppPreferences.setLanguage(context, "en") },
                            label = { Text("EN") }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Metadata Providers",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetadataOption("AniList", "GraphQL masivo para Manhwa/Manga")
            MetadataOption("Kitsu", "REST API para sinopsis y portadas")
            MetadataOption("Jikan (MAL)", "Popularidad y rankings de MyAnimeList")
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "App Info",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("VantInk v1.0.0", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MetadataOption(name: String, description: String) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = { Text(description) },
        trailingContent = { Switch(checked = true, onCheckedChange = {}) }
    )
}
