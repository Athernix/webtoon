package com.example.vantink.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.ExtensionAction
import com.example.vantink.domain.model.Webtoon

private val AmoledBlack = Color(0xFF000000)
private val CardBlack = Color(0xFF090909)
private val Accent = Color(0xFF37D4FF)
private val SoftRed = Color(0xFFFF5570)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceScreen(
    viewModel: SourceViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedLang by viewModel.selectedLang.collectAsStateWithLifecycle()
    val activeExtensions by viewModel.activeExtensions.collectAsStateWithLifecycle()
    val exploreQuery by viewModel.exploreQuery.collectAsStateWithLifecycle()
    val exploreState by viewModel.exploreState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val tabs = listOf("es" to "Español", "en" to "Inglés", "all" to "Multilenguaje")

    Scaffold(
        containerColor = AmoledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Tienda de Fuentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Sync, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StoreSearchHeader(
                    query = query,
                    onQueryChange = viewModel::updateQuery,
                    tabs = tabs,
                    selectedLang = selectedLang,
                    onLangSelected = viewModel::selectLanguage
                )
            }

            item {
                ExploreSection(
                    activeExtensions = activeExtensions,
                    query = exploreQuery,
                    state = exploreState,
                    onQueryChange = viewModel::updateExploreQuery,
                    onSearch = viewModel::globalSearch
                )
            }

            item {
                Text(
                    text = "Fuentes disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            when (val state = uiState) {
                ExtensionUiState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }

                is ExtensionUiState.Error -> item {
                    ErrorPanel(message = state.message, onRetry = viewModel::refresh)
                }

                is ExtensionUiState.Success -> items(
                    items = state.list,
                    key = { it.pkgName }
                ) { extension ->
                    ExtensionStoreCard(
                        extension = extension,
                        onInstall = {
                            viewModel.activate(extension)
                            if (extension.apkUrl.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extension.apkUrl)))
                            }
                        },
                        onOpen = {
                            if (extension.baseUrl.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extension.baseUrl)))
                            }
                        },
                        onDelete = { viewModel.remove(extension) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    tabs: List<Pair<String, String>>,
    selectedLang: String,
    onLangSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("TMO, MangaDex, Lectormanga...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBlack,
                unfocusedContainerColor = CardBlack,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == selectedLang }.coerceAtLeast(0),
            containerColor = AmoledBlack,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEach { (lang, label) ->
                Tab(
                    selected = selectedLang == lang,
                    onClick = { onLangSelected(lang) },
                    text = { Text(label, maxLines = 1) },
                    selectedContentColor = Accent,
                    unselectedContentColor = Color(0xFF8A8A8A)
                )
            }
        }
    }
}

@Composable
private fun ExtensionStoreCard(
    extension: Extension,
    onInstall: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val action = when {
        !extension.isActive -> ExtensionAction.Install
        extension.installedVersion != null && extension.installedVersion != extension.version -> ExtensionAction.Update
        else -> ExtensionAction.Manage
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = extension.iconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF171717)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = extension.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    LangBadge(extension.lang)
                    if (extension.isMetaProvider) TypeBadge("META", Accent)
                    if (extension.isDirectory) TypeBadge("DIR", Color(0xFFB4F56C))
                }
                Text(
                    text = extension.pkgName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E8E8E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        extension.isMetaProvider -> "Proveedor de metadatos y catalogo inicial"
                        extension.isDirectory -> "Directorio EverythingMoe para descubrir fuentes"
                        else -> "Instalada ${extension.installedVersion ?: "-"}  |  Disponible ${extension.version}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB7B7B7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedContent(targetState = action, label = "extension-action") { current ->
                when (current) {
                    ExtensionAction.Install -> OutlinedButton(
                        onClick = if (extension.isDirectory) onOpen else onInstall,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Accent)
                    ) {
                        Icon(
                            if (extension.isDirectory) Icons.Rounded.Public else Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(if (extension.isDirectory) "Abrir" else "Instalar")
                    }

                    ExtensionAction.Update -> Button(
                        onClick = onInstall,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)
                    ) {
                        Text("Actualizar")
                    }

                    ExtensionAction.Manage -> Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Ajustes", tint = Accent)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar", tint = SoftRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LangBadge(lang: String) {
    val color = when (lang.lowercase()) {
        "es" -> Accent
        "en" -> SoftRed
        else -> Color(0xFFB4F56C)
    }
    Surface(
        modifier = Modifier.padding(start = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = lang.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TypeBadge(text: String, color: Color) {
    Surface(
        modifier = Modifier.padding(start = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ExploreSection(
    activeExtensions: List<Extension>,
    query: String,
    state: ExploreUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050505)),
        border = BorderStroke(1.dp, Color(0xFF1C1C1C))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Explorar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${activeExtensions.take(4).size} fuentes activas en búsqueda global",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9C9C9C)
                    )
                }
                activeExtensions.take(4).forEach {
                    AssistChip(
                        onClick = {},
                        label = { Text(it.lang.uppercase()) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("Buscar manhwa") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CardBlack,
                        unfocusedContainerColor = CardBlack,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Button(
                    onClick = onSearch,
                    enabled = activeExtensions.isNotEmpty() && query.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = "Buscar")
                }
            }

            when (state) {
                ExploreUiState.Idle -> Unit
                ExploreUiState.Loading -> CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
                is ExploreUiState.Error -> Text(state.message, color = SoftRed)
                is ExploreUiState.Success -> ExploreGrid(state.list)
            }
        }
    }
}

@Composable
private fun ExploreGrid(items: List<Webtoon>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(112.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp),
        contentPadding = PaddingValues(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { item ->
            WebtoonResultCard(item)
        }
    }
}

@Composable
private fun WebtoonResultCard(item: Webtoon) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(1.5.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.86f))
                        )
                    )
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = SoftRed)
        Text(message, color = SoftRed)
        TextButton(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
