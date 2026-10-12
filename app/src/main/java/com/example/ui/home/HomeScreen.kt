package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.model.StreamSource
import com.example.ui.theme.StreamCardOverlay
import com.example.ui.theme.StreamDarkBackground
import com.example.ui.theme.StreamDarkBorder
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamDarkSurfaceVariant
import com.example.ui.theme.StreamGold
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.CatalogViewModel

@Composable
fun HomeScreen(
    viewModel: CatalogViewModel,
    onOpenMedia: (MediaItem) -> Unit,
    onQuickPlay: (MediaItem, StreamSource) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val mediaItems by viewModel.filteredMediaItems.collectAsState()
    val rawItems by viewModel.rawMediaItems.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    val featuredItem = rawItems.firstOrNull()

    val categories = listOf("All", "Animation", "Sci-Fi", "Documentary", "Space & Live", "Custom Stream")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StreamDarkBackground)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text("Search movies, shows, channels...", color = StreamTextSecondary, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = StreamPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = StreamTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StreamDarkSurface,
                        unfocusedContainerColor = StreamDarkSurface,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = StreamDarkBorder,
                        focusedTextColor = StreamTextPrimary,
                        unfocusedTextColor = StreamTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSelectCategory(cat) },
                        label = {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else StreamTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StreamPrimary,
                            containerColor = StreamDarkSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) StreamPrimary else StreamDarkBorder
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }
        }

        // Hero Spotlight Banner (when not searching)
        if (searchQuery.isBlank() && selectedCategory == "All" && featuredItem != null) {
            item {
                HeroBanner(
                    mediaItem = featuredItem,
                    onOpen = { onOpenMedia(featuredItem) },
                    onPlay = {
                        val src = featuredItem.streamSources.firstOrNull() ?: StreamSource("Direct", "", "Auto")
                        onQuickPlay(featuredItem, src)
                    }
                )
            }
        }

        // Continue Watching Swimlane (if history exists and not searching)
        if (searchQuery.isBlank() && watchHistory.isNotEmpty()) {
            item {
                ContinueWatchingSection(
                    historyList = watchHistory,
                    allItems = rawItems,
                    onOpenMedia = onOpenMedia,
                    onDeleteHistory = { viewModel.deleteWatchHistory(it) }
                )
            }
        }

        // Main Catalog Items / Categorized Sections
        if (mediaItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No streams match your filter",
                            color = StreamTextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.onSearchQueryChange("")
                                viewModel.onSelectCategory("All")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StreamDarkSurfaceVariant)
                        ) {
                            Text("Reset Filters", color = StreamPrimary)
                        }
                    }
                }
            }
        } else if (searchQuery.isNotBlank() || selectedCategory != "All") {
            // Filtered Grid / Column view
            item {
                Text(
                    text = "Results (${mediaItems.size})",
                    color = StreamTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(mediaItems.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            MediaCard(mediaItem = item, onClick = { onOpenMedia(item) })
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Default Categorized Swimlanes
            val animationItems = rawItems.filter { it.category == "Animation" }
            val sciFiItems = rawItems.filter { it.category == "Sci-Fi" }
            val docItems = rawItems.filter { it.category == "Documentary" || it.category == "Space & Live" }
            val customItems = rawItems.filter { it.category == "Custom Stream" }

            if (animationItems.isNotEmpty()) {
                item {
                    MediaSwimlane(
                        title = "Animation & Shorts",
                        subtitle = "Open source animated masterworks",
                        items = animationItems,
                        onOpenMedia = onOpenMedia
                    )
                }
            }

            if (sciFiItems.isNotEmpty()) {
                item {
                    MediaSwimlane(
                        title = "Sci-Fi & Cyberpunk",
                        subtitle = "High concept futuristic adventures",
                        items = sciFiItems,
                        onOpenMedia = onOpenMedia
                    )
                }
            }

            if (docItems.isNotEmpty()) {
                item {
                    MediaSwimlane(
                        title = "Nature & Live Feeds",
                        subtitle = "Panoramic vistas and live test channels",
                        items = docItems,
                        onOpenMedia = onOpenMedia
                    )
                }
            }

            if (customItems.isNotEmpty()) {
                item {
                    MediaSwimlane(
                        title = "Imported Custom Streams",
                        subtitle = "Loaded from user-added repositories",
                        items = customItems,
                        onOpenMedia = onOpenMedia
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    mediaItem: MediaItem,
    onOpen: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(280.dp)
            .clickable { onOpen() }
            .testTag("hero_banner_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = mediaItem.backdropUrl,
                contentDescription = mediaItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66000000),
                                Color(0xF0090A0F)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StreamPrimary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StreamPrimary)
                    ) {
                        Text(
                            text = "FEATURED",
                            color = StreamPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StreamGold.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = StreamGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "%.1f".format(mediaItem.rating), color = StreamGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = mediaItem.category,
                        color = StreamTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = mediaItem.title,
                    color = StreamTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = mediaItem.description,
                    color = StreamTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("hero_play_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stream Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StreamCardOverlay,
                        modifier = Modifier
                            .height(40.dp)
                            .clickable { onOpen() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text("Details", color = StreamTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    historyList: List<WatchHistoryEntity>,
    allItems: List<MediaItem>,
    onOpenMedia: (MediaItem) -> Unit,
    onDeleteHistory: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Continue Watching",
                color = StreamTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${historyList.size} in progress",
                color = StreamTextSecondary,
                fontSize = 12.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyList) { item ->
                val matched = allItems.find { it.id == item.mediaId }
                val progress = if (item.durationMs > 0) item.lastPositionMs.toFloat() / item.durationMs.toFloat() else 0f

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                    modifier = Modifier
                        .width(200.dp)
                        .clickable {
                            matched?.let { onOpenMedia(it) }
                        }
                        .testTag("continue_card_${item.mediaId}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                        ) {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Quick Play overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(38.dp)
                                    .background(Color(0x99000000), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(22.dp))
                            }

                            // Dismiss button
                            IconButton(
                                onClick = { onDeleteHistory(item.mediaId) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .padding(4.dp)
                                    .background(Color(0x99000000), CircleShape)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Progress line
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = StreamPrimary,
                            trackColor = StreamDarkSurfaceVariant
                        )

                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = item.title,
                                color = StreamTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% watched",
                                color = StreamPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaSwimlane(
    title: String,
    subtitle: String,
    items: List<MediaItem>,
    onOpenMedia: (MediaItem) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = title,
                color = StreamTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = StreamTextSecondary,
                fontSize = 12.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MediaCard(
                    mediaItem = item,
                    onClick = { onOpenMedia(item) },
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}

@Composable
fun MediaCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
        modifier = modifier
            .clickable { onClick() }
            .testTag("media_card_${mediaItem.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = mediaItem.posterUrl,
                    contentDescription = mediaItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Quality or Live Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (mediaItem.isLive) Color(0xFFEF4444) else Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (mediaItem.isLive) "LIVE" else mediaItem.streamSources.firstOrNull()?.quality ?: "HD",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Rating Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StreamGold, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "%.1f".format(mediaItem.rating), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = mediaItem.title,
                    color = StreamTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mediaItem.category,
                        color = StreamPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = mediaItem.releaseYear,
                        color = StreamTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
