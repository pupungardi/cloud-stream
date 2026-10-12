package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.model.StreamSource
import com.example.ui.details.MediaDetailSheet
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.player.CloudStreamPlayer
import com.example.ui.sources.SourcesScreen
import com.example.ui.theme.StreamDarkBackground
import com.example.ui.theme.StreamDarkBorder
import com.example.ui.theme.StreamDarkSurface
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamTextPrimary
import com.example.ui.theme.StreamTextSecondary
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.PlayerViewModel

enum class MainTab(val title: String) {
    DISCOVER("Discover"),
    LIBRARY("Library"),
    SOURCES("Sources")
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainScreen(
    catalogViewModel: CatalogViewModel,
    playerViewModel: PlayerViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activePlayerMedia by remember { mutableStateOf<MediaItem?>(null) }
    val selectedMediaForDetail by catalogViewModel.selectedMedia.collectAsState()
    val watchHistory by catalogViewModel.watchHistory.collectAsState()
    val bookmarks by catalogViewModel.bookmarks.collectAsState()

    // Handle back button if player is active
    if (activePlayerMedia != null) {
        CloudStreamPlayer(
            viewModel = playerViewModel,
            onClose = {
                activePlayerMedia = null
            }
        )
        return
    }

    // Handle back button from non-home tabs
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        bottomBar = {
            // Apple Glass Floating Navigation Bar Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(33.dp),
                            spotColor = StreamPrimary.copy(alpha = 0.35f),
                            ambientColor = Color.Black
                        ),
                    shape = RoundedCornerShape(33.dp),
                    color = Color(0xCC121622), // Frosted dark glass translucent background
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MainTab.entries.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            val icon = when (tab) {
                                MainTab.DISCOVER -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                MainTab.LIBRARY -> if (isSelected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary
                                MainTab.SOURCES -> if (isSelected) Icons.Filled.CloudDownload else Icons.Outlined.CloudDownload
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(
                                        if (isSelected) StreamPrimary.copy(alpha = 0.18f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTabIndex = index }
                                    .testTag("nav_tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) StreamPrimary else StreamTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.title,
                                            color = StreamPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = StreamDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tabIdx ->
                when (MainTab.entries[tabIdx]) {
                    MainTab.DISCOVER -> {
                        HomeScreen(
                            viewModel = catalogViewModel,
                            onOpenMedia = { item ->
                                catalogViewModel.openMediaDetails(item)
                            },
                            onQuickPlay = { item, source ->
                                activePlayerMedia = item
                                playerViewModel.loadMedia(item, source)
                            },
                            onNavigateToSources = {
                                selectedTabIndex = 2
                            }
                        )
                    }
                    MainTab.LIBRARY -> {
                        LibraryScreen(
                            viewModel = catalogViewModel,
                            onOpenMedia = { item ->
                                catalogViewModel.openMediaDetails(item)
                            },
                            onNavigateDiscover = {
                                selectedTabIndex = 0
                            }
                        )
                    }
                    MainTab.SOURCES -> {
                        SourcesScreen(
                            viewModel = catalogViewModel
                        )
                    }
                }
            }
        }
    }

    // Media Detail Modal Bottom Sheet
    selectedMediaForDetail?.let { media ->
        val historyEntry = watchHistory.find { it.mediaId == media.id }
        val isBookmarked = bookmarks.any { it.mediaId == media.id }
        MediaDetailSheet(
            mediaItem = media,
            watchHistory = historyEntry,
            isBookmarked = isBookmarked,
            onDismiss = { catalogViewModel.closeMediaDetails() },
            onPlay = { item, source, startFromBeginning ->
                catalogViewModel.closeMediaDetails()
                activePlayerMedia = item
                playerViewModel.loadMedia(item, source, startFromBeginning)
            },
            onToggleBookmark = {
                catalogViewModel.toggleBookmark(media, isBookmarked)
            }
        )
    }
}
