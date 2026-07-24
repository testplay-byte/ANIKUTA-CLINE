package app.confused.anikuta.feature.animedetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.confused.anikuta.core.common.repository.AnimeRepository
import app.confused.anikuta.core.common.repository.CategoryRepository
import app.confused.anikuta.core.common.repository.EpisodeRepository
import app.confused.anikuta.core.designsystem.component.CollapsingHeader
import app.confused.anikuta.core.designsystem.theme.RobotoFamily
import app.confused.anikuta.data.extension.matcher.SourceMatcher
import coil3.compose.AsyncImage
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode

/**
 * Extension-only details page — for anime not on AniList.
 *
 * Shows the SAnime data (title, cover, description, genres, status) + the
 * episode list from the extension source. The user can save to library,
 * watch episodes, and refresh the episode list.
 *
 * Per user: "the extension provides quite a lot of details too, like the
 * title, the cover, the genres, the synopsis, and the details and
 * information of the anime too."
 */
@Composable
fun ExtensionDetailScreen(
    source: AnimeCatalogueSource,
    sAnime: SAnime,
    onBack: () -> Unit,
    onOpenEpisode: (SEpisode, AnimeCatalogueSource, List<SEpisode>) -> Unit,
) {
    val context = LocalContext.current

    val animeRepository: AnimeRepository = remember { org.koin.core.context.GlobalContext.get().get() }
    val categoryRepository: CategoryRepository = remember { org.koin.core.context.GlobalContext.get().get() }
    val episodeRepository: EpisodeRepository = remember { org.koin.core.context.GlobalContext.get().get() }

    @Suppress("UNCHECKED_CAST")
    val vm: ExtensionDetailViewModel = viewModel(
        key = "ext_detail_${source.id}_${sAnime.url}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExtensionDetailViewModel(
                    source = source,
                    sAnime = sAnime,
                    animeRepository = animeRepository,
                    categoryRepository = categoryRepository,
                    episodeRepository = episodeRepository,
                    appContext = context.applicationContext,
                ) as T
        },
    )

    val animeState by vm.animeState.collectAsState()
    val episodeState by vm.episodeState.collectAsState()
    val isSaved by vm.isSaved.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()
    val currentMatch by vm.currentMatch.collectAsState()

    val scrollState = androidx.compose.foundation.rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = animeState) {
            is ExtensionDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ExtensionDetailState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ExtensionDetailState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar with back + save + refresh
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                        Text(
                            text = state.anime.title,
                            fontFamily = RobotoFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = { vm.toggleSave() }) {
                            Icon(
                                if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                "Save",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { vm.refresh() }) {
                            Icon(Icons.Filled.Refresh, "Refresh")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp),
                    ) {
                        // Cover image
                        item {
                            if (!state.anime.coverUrl.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                ) {
                                    AsyncImage(
                                        model = state.anime.coverUrl,
                                        contentDescription = state.anime.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    0.5f to Color.Transparent,
                                                    1.0f to MaterialTheme.colorScheme.background,
                                                ),
                                            ),
                                    )
                                }
                            }
                        }

                        // Title
                        item {
                            Text(
                                text = state.anime.title,
                                fontFamily = RobotoFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        // Source name
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = state.anime.sourceName,
                                    fontFamily = RobotoFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }

                        // Description
                        if (!state.anime.description.isNullOrBlank()) {
                            item {
                                var expanded = remember { androidx.compose.runtime.mutableStateOf(false) }
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(
                                        text = "Synopsis",
                                        fontFamily = RobotoFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = state.anime.description!!.replace(Regex("<[^>]*>"), ""),
                                        fontFamily = RobotoFamily,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = if (expanded.value) Int.MAX_VALUE else 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (state.anime.description!!.length > 100) {
                                        Text(
                                            text = if (expanded.value) "Show less" else "Show more",
                                            fontFamily = RobotoFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp).clickable { expanded.value = !expanded.value },
                                        )
                                    }
                                }
                            }
                        }

                        // Genres
                        if (state.anime.genre.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Genres: ${state.anime.genre.joinToString(", ")}",
                                    fontFamily = RobotoFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                )
                            }
                        }

                        // Episodes section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Episode header
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Episodes",
                                    fontFamily = RobotoFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                if (episodeState is EpisodeState.Loaded) {
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(50),
                                    ) {
                                        Text(
                                            text = "${(episodeState as EpisodeState.Loaded).episodes.size}",
                                            fontFamily = RobotoFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Episode list
                        when (episodeState) {
                            is EpisodeState.Idle -> {}
                            is EpisodeState.Searching -> {
                                item { Text("Searching...", modifier = Modifier.padding(16.dp)) }
                            }
                            is EpisodeState.Loading -> {
                                item {
                                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            is EpisodeState.Loaded -> {
                                val episodes = (episodeState as EpisodeState.Loaded).episodes
                                items(episodes.size) { index ->
                                    val ep = episodes[index]
                                    // Simple episode row for extension-only
                                    val displayPrefs = rememberExtensionDisplayPrefs()
                                    ExtensionEpisodeRow(
                                        episode = ep,
                                        displayPrefs = displayPrefs,
                                        onClick = { onOpenEpisode(ep, source, episodes) },
                                    )
                                }
                            }
                            is EpisodeState.NoMatch -> {
                                item { Text("No episodes found", modifier = Modifier.padding(16.dp)) }
                            }
                            is EpisodeState.Error -> {
                                item { Text("Error: ${(episodeState as EpisodeState.Error).message}", modifier = Modifier.padding(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Simple display prefs for extension-only episode rows (uses defaults). */
@Composable
private fun rememberExtensionDisplayPrefs(): EpisodeDisplayPrefs {
    return remember { EpisodeDisplayPrefs() }
}

/** A simplified episode row for the extension-only details page. */
@Composable
private fun ExtensionEpisodeRow(
    episode: SEpisode,
    displayPrefs: EpisodeDisplayPrefs,
    onClick: () -> Unit,
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val epNumText = "EP ${episode.episode_number.toInt()}"
    val displayTitle = episode.name.ifBlank { "Episode ${episode.episode_number.toInt()}" }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // EP badge (themed primary green)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = epNumText,
                    fontFamily = RobotoFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = displayTitle,
                fontFamily = RobotoFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = displayPrefs.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.items(count: Int, itemContent: @Composable (Int) -> T) {
    items(count, key = { it }, itemContent = { itemContent(it) })
}
