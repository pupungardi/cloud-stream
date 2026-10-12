package com.example.ui.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.model.MediaItem
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.LibrarySegment
import com.example.ui.viewmodel.LibraryViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: CatalogViewModel,
    onOpenMedia: (MediaItem) -> Unit,
    onNavigateSources: () -> Unit
) {
    val segment by viewModel.librarySegment.collectAsState()
    val viewMode by viewModel.libraryViewMode.collectAsState()
    val selectedType by viewModel.librarySelectedType.collectAsState()
    val sortOrder by viewModel.librarySortOrder.collectAsState()
    val availableTypes by viewModel.libraryAvailableTypes.collectAsState()
    val items by viewModel.libraryItems.collectAsState()
    val groupedItems by viewModel.libraryGroupedItems.collectAsState()

    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalBottomPadding = navBarHeight + 90.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("library_screen")
    ) {
        LibraryHeader(
            viewMode = viewMode,
            onToggleViewMode = {
                viewModel.setLibraryViewMode(
                    if (viewMode == LibraryViewMode.ROW) LibraryViewMode.GRID else LibraryViewMode.ROW
                )
            }
        )

        LibrarySegmentChips(
            selectedSegment = segment,
            onSelectSegment = { viewModel.setLibrarySegment(it) }
        )

        LibraryFilterRow(
            viewMode = viewMode,
            availableTypes = availableTypes,
            selectedType = selectedType,
            onSelectType = { viewModel.setLibrarySelectedType(it) },
            sortOrder = sortOrder,
            onSelectSort = { viewModel.setLibrarySortOrder(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "view_mode_transition"
            ) { mode ->
                if (mode == LibraryViewMode.ROW) {
                    LibraryRowsContent(
                        groupedItems = groupedItems,
                        onOpenMedia = onOpenMedia,
                        contentPadding = PaddingValues(bottom = totalBottomPadding, top = 8.dp)
                    )
                } else {
                    LibraryGridContent(
                        items = items,
                        onOpenMedia = onOpenMedia,
                        onNavigateSources = onNavigateSources,
                        isCloudEmpty = segment == LibrarySegment.CLOUD && items.isEmpty(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = totalBottomPadding, top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryHeader(
    viewMode: LibraryViewMode,
    onToggleViewMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pustaka",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .clickable(onClick = onToggleViewMode)
                .testTag("toggle_view_mode_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (viewMode == LibraryViewMode.ROW) Icons.Default.GridView else Icons.Default.ViewStream,
                contentDescription = if (viewMode == LibraryViewMode.ROW) "Beralih ke mode Grid" else "Beralih ke mode Baris",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LibrarySegmentChips(
    selectedSegment: LibrarySegment,
    onSelectSegment: (LibrarySegment) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LibrarySegment.entries.forEach { segment ->
            val isSelected = selectedSegment == segment
            val title = if (segment == LibrarySegment.TERSIMPAN) "Tersimpan" else "Cloud"
            val bg = if (isSelected) Color(0xFF12304D) else Color(0xFF1C1C1E)
            val border = if (isSelected) BorderStroke(1.dp, Color(0xFF2D6CB5)) else null
            val textColor = if (isSelected) Color.White else Color(0xFFAAAAAA)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bg,
                border = border,
                modifier = Modifier
                    .height(42.dp)
                    .clickable { onSelectSegment(segment) }
                    .testTag("segment_${segment.name.lowercase()}")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryFilterRow(
    viewMode: LibraryViewMode,
    availableTypes: List<String>,
    selectedType: String,
    onSelectType: (String) -> Unit,
    sortOrder: String,
    onSelectSort: (String) -> Unit
) {
    val sortOptions = listOf("Recently added", "Title A-Z", "Title Z-A", "Year (terbaru)", "Recently watched")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (viewMode == LibraryViewMode.GRID) {
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { expanded = true }
                        .testTag("type_dropdown_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedType, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1C1C1E))
                ) {
                    availableTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = Color.White) },
                            onClick = {
                                onSelectType(type)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            var expanded by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1C1C1E),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { expanded = true }
                    .testTag("sort_dropdown_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = sortOrder, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1C1C1E))
            ) {
                sortOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = Color.White) },
                        onClick = {
                            onSelectSort(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryRowsContent(
    groupedItems: Map<String, List<MediaItem>>,
    onOpenMedia: (MediaItem) -> Unit,
    contentPadding: PaddingValues
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = screenWidth * 0.62f

    if (groupedItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF555555), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Belum ada yang tersimpan", color = Color(0xFF888888), fontSize = 15.sp)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        groupedItems.forEach { (category, items) ->
            if (items.isNotEmpty()) {
                item(key = category) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = category,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                LibraryBackdropCard(
                                    item = item,
                                    width = cardWidth,
                                    onClick = { onOpenMedia(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryBackdropCard(
    item: MediaItem,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier
            .width(width)
            .height(width * 0.5625f)
            .clickable(onClick = onClick)
            .testTag("backdrop_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!item.backdropUrl.isNullOrBlank() || !item.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.backdropUrl ?: item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 50f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryGridContent(
    items: List<MediaItem>,
    onOpenMedia: (MediaItem) -> Unit,
    onNavigateSources: () -> Unit,
    isCloudEmpty: Boolean,
    contentPadding: PaddingValues
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF555555), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Belum ada item", color = Color(0xFF888888), fontSize = 15.sp)
                if (isCloudEmpty) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateSources,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6CB5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambah sumber", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            LibraryPosterCard(
                item = item,
                onClick = { onOpenMedia(item) }
            )
        }
    }
}

@Composable
fun LibraryPosterCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable(onClick = onClick)
            .testTag("poster_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = item.posterUrl ?: item.backdropUrl
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF333333), Color(0xFF111111))
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Previews
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLibraryRowMode() {
    val dummyItems = listOf(
        MediaItem(id = "1", title = "Cyberpunk Series", category = "Series", streamSources = emptyList()),
        MediaItem(id = "2", title = "Sci-Fi Movie", category = "Movie", streamSources = emptyList())
    )
    Column(modifier = Modifier.background(Color.Black).fillMaxSize()) {
        LibraryHeader(viewMode = LibraryViewMode.ROW, onToggleViewMode = {})
        LibrarySegmentChips(selectedSegment = LibrarySegment.TERSIMPAN, onSelectSegment = {})
        LibraryFilterRow(
            viewMode = LibraryViewMode.ROW,
            availableTypes = listOf("All types", "Movie", "Series"),
            selectedType = "All types",
            onSelectType = {},
            sortOrder = "Recently added",
            onSelectSort = {}
        )
        LibraryRowsContent(
            groupedItems = dummyItems.groupBy { it.category },
            onOpenMedia = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLibraryGridMode() {
    val dummyItems = listOf(
        MediaItem(id = "1", title = "Action Movie 1", category = "Movie", streamSources = emptyList()),
        MediaItem(id = "2", title = "Action Movie 2", category = "Movie", streamSources = emptyList()),
        MediaItem(id = "3", title = "Drama Series", category = "Series", streamSources = emptyList())
    )
    Column(modifier = Modifier.background(Color.Black).fillMaxSize()) {
        LibraryHeader(viewMode = LibraryViewMode.GRID, onToggleViewMode = {})
        LibrarySegmentChips(selectedSegment = LibrarySegment.CLOUD, onSelectSegment = {})
        LibraryFilterRow(
            viewMode = LibraryViewMode.GRID,
            availableTypes = listOf("All types", "Movie", "Series"),
            selectedType = "Movie",
            onSelectType = {},
            sortOrder = "Title A-Z",
            onSelectSort = {}
        )
        LibraryGridContent(
            items = dummyItems,
            onOpenMedia = {},
            onNavigateSources = {},
            isCloudEmpty = false,
            contentPadding = PaddingValues(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLibraryEmptyState() {
    Column(modifier = Modifier.background(Color.Black).fillMaxSize()) {
        LibraryHeader(viewMode = LibraryViewMode.ROW, onToggleViewMode = {})
        LibrarySegmentChips(selectedSegment = LibrarySegment.CLOUD, onSelectSegment = {})
        LibraryFilterRow(
            viewMode = LibraryViewMode.ROW,
            availableTypes = listOf("All types"),
            selectedType = "All types",
            onSelectType = {},
            sortOrder = "Recently added",
            onSelectSort = {}
        )
        LibraryGridContent(
            items = emptyList(),
            onOpenMedia = {},
            onNavigateSources = {},
            isCloudEmpty = true,
            contentPadding = PaddingValues(16.dp)
        )
    }
}
