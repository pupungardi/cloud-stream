package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
            NavigationBar(
                containerColor = StreamDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .background(StreamDarkSurface)
                    .testTag("main_bottom_nav")
            ) {
                MainTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            val icon = when (tab) {
                                MainTab.DISCOVER -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                MainTab.LIBRARY -> if (isSelected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary
                                MainTab.SOURCES -> if (isSelected) Icons.Filled.CloudDownload else Icons.Outlined.CloudDownload
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color.Black else StreamTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) StreamPrimary else StreamTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = StreamPrimary,
                            selectedIconColor = Color.Black,
                            unselectedIconColor = StreamTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
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
