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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

    val featuredItems = rawItems.take(5)

    val categories = remember(rawItems) {
        val distinctCats = rawItems.map { it.category }.filter { it.isNotBlank() }.distinct()
        listOf("All") + distinctCats
    }

    val groupedCategories = remember(rawItems) {
        rawItems.groupBy { it.category }
    }

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
                modifier = Modifier.padding(bottom = 10.dp)
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

        // Hero Spotlight Carousel (Desain persis seperti referensi dengan logo text)
        if (searchQuery.isBlank() && selectedCategory == "All" && featuredItems.isNotEmpty()) {
            item {
                HeroCarousel(
                    featuredItems = featuredItems,
                    onOpenMedia = onOpenMedia,
                    onQuickPlay = onQuickPlay
                )
            }
        }

        // Continue Watching Swimlane (jika ada riwayat)
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
                        if (rawItems.isEmpty()) {
                            Text(
                                text = "No stream sources added",
                                color = StreamTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Import your streaming feeds or URLs in the Sources tab to start watching.",
                                color = StreamTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        } else {
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
            // Dynamic Categorized Swimlanes
            groupedCategories.forEach { (categoryName, itemsInCategory) ->
                if (itemsInCategory.isNotEmpty()) {
                    item {
                        MediaSwimlane(
                            title = categoryName,
                            subtitle = "${itemsInCategory.size} ${if (itemsInCategory.size == 1) "stream" else "streams"}",
                            items = itemsInCategory,
                            onOpenMedia = onOpenMedia
                        )
                    }
                }
            }
        }
    }
}

/**
 * Karousel Hero dengan desain peek (kartu berikutnya mengintip di sisi kanan)
 * dan Logo Text bergaya poster sinematik sesuai referensi desain.
 */
@Composable
fun HeroCarousel(
    featuredItems: List<MediaItem>,
    onOpenMedia: (MediaItem) -> Unit,
    onQuickPlay: (MediaItem, StreamSource) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredItems.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { featuredItems.size })

    // Auto-advance setiap 6 detik jika pengguna tidak sedang menggeser
    LaunchedEffect(pagerState) {
        while (isActive) {
            delay(6000)
            if (!pagerState.isScrollInProgress && featuredItems.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % featuredItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .testTag("hero_carousel_pager")
        ) { page ->
            val item = featuredItems[page]
            HeroCard(
                mediaItem = item,
                onClick = { onOpenMedia(item) },
                onPlay = {
                    val src = item.streamSources.firstOrNull() ?: StreamSource("Direct", "", "Auto")
                    onQuickPlay(item, src)
                }
            )
        }

        // Titik indikator halaman yang halus di bawah karousel
        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(featuredItems.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(
                            width = if (isSelected) 18.dp else 6.dp,
                            height = 6.dp
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) StreamPrimary else StreamDarkSurfaceVariant)
                )
            }
        }
    }
}

/**
 * Kartu tunggal pada Karousel Hero dengan sudut melengkung,
 * gradien bawah elegan, tombol quick-play, dan Logo Text.
 */
@Composable
fun HeroCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .testTag("hero_card_${mediaItem.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!mediaItem.backdropUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mediaItem.backdropUrl,
                    contentDescription = mediaItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                MediaPosterPlaceholder(title = mediaItem.title, modifier = Modifier.fillMaxSize())
            }

            // Gradien bawah sinematik agar Logo Text sangat kontras dan terbaca jelas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x33000000),
                                Color(0xDD090A0F)
                            ),
                            startY = 60f
                        )
                    )
            )

            // Quick Play icon melayang di kanan atas
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Stream Now",
                    tint = StreamPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Kategori kecil di kiri atas
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x99000000),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = mediaItem.category.uppercase(),
                    color = StreamPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // Stylized Logo Text di bagian bawah tengah kartu
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                HeroLogoText(mediaItem = mediaItem)
            }
        }
    }
}

/**
 * Render Logo Text bergaya sinematik untuk judul media.
 */
@Composable
fun HeroLogoText(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mediaItem.title.uppercase(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.5.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(1f, 2f),
                    blurRadius = 6f
                )
            )
        )
        if (mediaItem.category.isNotBlank()) {
            Text(
                text = mediaItem.category.uppercase(),
                color = StreamPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
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
                            if (!item.posterUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                MediaPosterPlaceholder(title = item.title, modifier = Modifier.fillMaxSize())
                            }

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
                if (!mediaItem.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = mediaItem.posterUrl,
                        contentDescription = mediaItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MediaPosterPlaceholder(title = mediaItem.title, modifier = Modifier.fillMaxSize())
                }

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

                // Rating Badge (hanya tampil jika rating tidak null)
                if (mediaItem.rating != null) {
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
                    if (!mediaItem.releaseYear.isNullOrBlank()) {
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
}

/**
 * Placeholder lokal elegan berbasis gradien dan inisial/ikon ketika URL poster bernilai null.
 */
@Composable
fun MediaPosterPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        StreamDarkSurfaceVariant,
                        StreamDarkSurface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = StreamPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
            if (title.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title.take(2).uppercase(),
                    color = StreamTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
