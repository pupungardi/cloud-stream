package com.example.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.model.StreamSource
import com.example.ui.home.MediaPosterPlaceholder
import com.example.ui.theme.StreamDarkBackground
import com.example.ui.theme.StreamDarkBorder
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamDarkSurfaceVariant
import com.example.ui.theme.StreamGold
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailSheet(
    mediaItem: MediaItem,
    watchHistory: WatchHistoryEntity?,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onPlay: (MediaItem, StreamSource, Boolean) -> Unit,
    onToggleBookmark: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StreamDarkBackground,
        dragHandle = null,
        modifier = Modifier.testTag("media_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (!mediaItem.backdropUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = mediaItem.backdropUrl,
                        contentDescription = mediaItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    MediaPosterPlaceholder(
                        title = mediaItem.title,
                        modifier = Modifier.matchParentSize()
                    )
                }

                // Scrim overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    StreamDarkBackground.copy(alpha = 0.5f),
                                    StreamDarkBackground
                                )
                            )
                        )
                )

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color(0x99000000), CircleShape)
                        .testTag("close_detail_sheet_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StreamTextPrimary
                    )
                }

                // Rating & Category Pill on backdrop
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mediaItem.rating != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreamGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StreamGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StreamGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "%.1f".format(mediaItem.rating), color = StreamGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (mediaItem.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreamDarkSurfaceVariant
                        ) {
                            Text(
                                text = mediaItem.category,
                                color = StreamPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!mediaItem.releaseYear.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreamDarkSurfaceVariant
                        ) {
                            Text(
                                text = mediaItem.releaseYear,
                                color = StreamTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (mediaItem.durationText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreamDarkSurfaceVariant
                        ) {
                            Text(
                                text = mediaItem.durationText,
                                color = StreamTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Body Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = mediaItem.title,
                    color = StreamTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("detail_title_text")
                )

                if (mediaItem.sourceRepo.isNotBlank()) {
                    Text(
                        text = "Source: ${mediaItem.sourceRepo}",
                        color = StreamTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                }

                // Resume Bar if watch history exists
                if (watchHistory != null && watchHistory.lastPositionMs > 1000) {
                    val progress = if (watchHistory.durationMs > 0) {
                        watchHistory.lastPositionMs.toFloat() / watchHistory.durationMs.toFloat()
                    } else 0f

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StreamDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Continue Watching",
                                    color = StreamPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatTimestamp(watchHistory.lastPositionMs),
                                    color = StreamTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = StreamPrimary,
                                trackColor = StreamDarkSurfaceVariant
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val defaultSource = mediaItem.streamSources.firstOrNull() ?: StreamSource("Direct", "", "Auto")

                    if (watchHistory != null && watchHistory.lastPositionMs > 1000) {
                        Button(
                            onClick = {
                                onPlay(mediaItem, defaultSource, false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("resume_play_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onPlay(mediaItem, defaultSource, true)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("restart_play_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = StreamTextPrimary)
                        }
                    } else {
                        Button(
                            onClick = {
                                onPlay(mediaItem, defaultSource, false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("start_play_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Now", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bookmark Button
                    OutlinedButton(
                        onClick = onToggleBookmark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (isBookmarked) StreamGold else StreamTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBookmarked) "Saved" else "Watchlist",
                            color = if (isBookmarked) StreamGold else StreamTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description / Stream Info (hanya tampil jika tidak kosong)
                if (mediaItem.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Stream Details",
                        color = StreamTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mediaItem.description,
                        color = StreamTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Available Stream Mirrors & Qualities
                Text(
                    text = "Streaming Sources & Qualities (${mediaItem.streamSources.size})",
                    color = StreamTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                mediaItem.streamSources.forEach { source ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StreamDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StreamDarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onPlay(mediaItem, source, false)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = source.label,
                                    color = StreamTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = source.quality,
                                    color = StreamPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play source",
                                tint = StreamPrimary
                            )
                        }
                    }
                }

                // Subtitle Info if present
                if (mediaItem.subtitles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Subtitles Available",
                        color = StreamTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mediaItem.subtitles.forEach { sub ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StreamDarkSurfaceVariant
                            ) {
                                Text(
                                    text = sub.language,
                                    color = StreamTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
