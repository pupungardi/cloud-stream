package com.example.ui.library

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaItem
import com.example.ui.home.MediaPosterPlaceholder
import com.example.ui.theme.StreamDarkBackground
import com.example.ui.theme.StreamDarkBorder
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamDarkSurfaceVariant
import com.example.ui.theme.StreamGold
import com.example.ui.theme.StreamPrimary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.CatalogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: CatalogViewModel,
    onOpenMedia: (MediaItem) -> Unit,
    onQuickPlay: (MediaItem, com.example.model.StreamSource) -> Unit,
    onNavigateDiscover: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val allItems by viewModel.rawMediaItems.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StreamDarkBackground)
            .testTag("library_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Library",
                color = StreamTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your favorite streams and viewing history",
                color = StreamTextSecondary,
                fontSize = 13.sp
            )
        }

        // Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StreamDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StreamGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = StreamGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "${bookmarks.size}", color = StreamTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Watchlist", color = StreamTextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StreamDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StreamPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = StreamPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "${watchHistory.size}", color = StreamTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Watched", color = StreamTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = StreamDarkBackground,
            contentColor = StreamPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Watchlist (${bookmarks.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_watchlist")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("History (${watchHistory.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_history")
            )
        }

        // Tab Content
        if (selectedTab == 0) {
            // Bookmarks Tab
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = StreamDarkSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Watchlist is empty",
                            color = StreamTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Save movies and streams to easily watch them later",
                            color = StreamTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )
                        Button(
                            onClick = onNavigateDiscover,
                            colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Discover Streams", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bookmarks) { b ->
                        val matched = allItems.find { it.id == b.mediaId }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    matched?.let { onOpenMedia(it) }
                                }
                                .testTag("bookmark_item_${b.mediaId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!b.posterUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = b.posterUrl,
                                        contentDescription = b.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp, 96.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    MediaPosterPlaceholder(
                                        title = b.title,
                                        modifier = Modifier
                                            .size(64.dp, 96.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = b.title,
                                        color = StreamTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StreamDarkSurfaceVariant
                                        ) {
                                            Text(
                                                text = b.category,
                                                color = StreamPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (b.rating > 0f) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = StreamGold, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(text = "%.1f".format(b.rating), color = StreamGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Duration: ${b.durationText}",
                                        color = StreamTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (matched != null && matched.streamSources.isNotEmpty()) {
                                        IconButton(
                                            onClick = { onQuickPlay(matched, matched.streamSources.first()) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(StreamPrimary.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = StreamPrimary, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            matched?.let { viewModel.toggleBookmark(it, true) }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Bookmark",
                                            tint = StreamTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // History Tab
            if (watchHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = StreamDarkSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No watch history yet",
                            color = StreamTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Play streams and your progress will automatically sync here",
                            color = StreamTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )
                        Button(
                            onClick = onNavigateDiscover,
                            colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Streaming", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showClearHistoryDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = StreamTextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear History", color = StreamTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    items(watchHistory) { h ->
                        val matched = allItems.find { it.id == h.mediaId }
                        val progress = if (h.durationMs > 0) h.lastPositionMs.toFloat() / h.durationMs.toFloat() else 0f
                        val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StreamDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    matched?.let { onOpenMedia(it) }
                                }
                                .testTag("history_item_${h.mediaId}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!h.posterUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = h.posterUrl,
                                            contentDescription = h.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp, 96.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        MediaPosterPlaceholder(
                                            title = h.title,
                                            modifier = Modifier
                                                .size(64.dp, 96.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = h.title,
                                            color = StreamTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Watched ${dateFormat.format(Date(h.updatedAt))}",
                                            color = StreamTextSecondary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                                        )
                                        Text(
                                            text = "${(progress * 100).toInt()}% completed",
                                            color = StreamPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (matched != null && matched.streamSources.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onQuickPlay(matched, matched.streamSources.first()) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(StreamPrimary.copy(alpha = 0.2f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = StreamPrimary, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteWatchHistory(h.mediaId) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = StreamTextSecondary)
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp),
                                    color = StreamPrimary,
                                    trackColor = StreamDarkSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Clear History Confirmation Dialog
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear All Watch History?", color = StreamTextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove all viewing progress from your device.", color = StreamTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearWatchHistory()
                            showClearHistoryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = StreamTextPrimary)
                    }
                },
                containerColor = StreamDarkSurface
            )
        }
    }
}
