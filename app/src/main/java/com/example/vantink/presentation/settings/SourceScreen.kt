package com.example.vantink.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceScreen(
    viewModel: SourceViewModel,
    onBack: () -> Unit
) {
    val installed by viewModel.installedSources.collectAsState()
    val discovery by viewModel.discoveryResults.collectAsState()
    val repositories by viewModel.repositories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var repoUrlInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extensions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Manage Repositories", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = repoUrlInput,
                            onValueChange = { repoUrlInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("https://repo.json") },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                if (repoUrlInput.isNotBlank()) {
                                    viewModel.addRepository(repoUrlInput)
                                    repoUrlInput = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors()
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Repo")
                        }
                    }
                }
            }

            items(repositories) { repo ->
                ListItem(
                    headlineContent = { Text(repo.url, style = MaterialTheme.typography.bodySmall) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { viewModel.fetchFromRepository(repo.url) }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Fetch")
                            }
                            IconButton(onClick = { viewModel.removeRepository(repo) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove Repo")
                            }
                        }
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search extensions...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (installed.isNotEmpty()) {
                item {
                    Text("Installed", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                
                val filteredInstalled = installed.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filteredInstalled) { source ->
                    SourceItem(
                        source = source,
                        isInstalled = true,
                        onToggle = { viewModel.toggleSource(source) },
                        onAction = { viewModel.uninstallSource(source) }
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (discovery.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Available", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }

                val filteredDiscovery = discovery.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filteredDiscovery) { source ->
                    val isAlreadyInstalled = installed.any { it.id == source.id }
                    if (!isAlreadyInstalled) {
                        SourceItem(
                            source = source,
                            isInstalled = false,
                            onToggle = {},
                            onAction = { viewModel.installSource(source) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SourceItem(
    source: com.example.vantink.data.local.entity.SourceEntity,
    isInstalled: Boolean,
    onToggle: () -> Unit,
    onAction: () -> Unit
) {
    ListItem(
        headlineContent = { Text(source.name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("${source.lang.uppercase()} • ${source.type}") },
        leadingContent = {
            if (source.iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = source.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(source.name.take(1).uppercase())
                    }
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isInstalled) {
                    Switch(checked = source.isEnabled, onCheckedChange = { onToggle() })
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onAction) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Uninstall", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Button(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Install")
                    }
                }
            }
        }
    )
}
