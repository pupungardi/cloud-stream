package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia = _selectedMedia.asStateFlow()

    val rawMediaItems: StateFlow<List<MediaItem>> = repository.getAllMediaItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customSources: StateFlow<List<CustomSourceEntity>> = repository.customSources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredMediaItems: StateFlow<List<MediaItem>> = combine(
        rawMediaItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true)

            val matchesCategory = category.isBlank() || item.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun openMediaDetails(item: MediaItem) {
        _selectedMedia.value = item
    }

    fun closeMediaDetails() {
        _selectedMedia.value = null
    }

    fun toggleBookmark(mediaItem: MediaItem, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(mediaItem, isBookmarked)
        }
    }

    fun deleteWatchHistory(mediaId: String) {
        viewModelScope.launch {
            repository.deleteWatchHistory(mediaId)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun addCustomSource(name: String, url: String, type: String = "DIRECT_STREAM") {
        viewModelScope.launch {
            repository.addCustomSource(name, url, type)
        }
    }

    fun toggleCustomSource(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleCustomSource(id, isEnabled)
        }
    }

    fun deleteCustomSource(id: String) {
        viewModelScope.launch {
            repository.deleteCustomSource(id)
        }
    }

    suspend fun testUrl(url: String): Boolean {
        return repository.testUrlConnectivity(url)
    }

    private val _librarySegment = MutableStateFlow(LibrarySegment.TERSIMPAN)
    val librarySegment: StateFlow<LibrarySegment> = _librarySegment.asStateFlow()

    private val _libraryViewMode = MutableStateFlow(LibraryViewMode.ROW)
    val libraryViewMode: StateFlow<LibraryViewMode> = _libraryViewMode.asStateFlow()

    private val _librarySelectedType = MutableStateFlow("All types")
    val librarySelectedType: StateFlow<String> = _librarySelectedType.asStateFlow()

    private val _librarySortOrder = MutableStateFlow("Recently added")
    val librarySortOrder: StateFlow<String> = _librarySortOrder.asStateFlow()

    fun setLibrarySegment(segment: LibrarySegment) {
        _librarySegment.value = segment
        _librarySelectedType.value = "All types"
    }

    fun setLibraryViewMode(mode: LibraryViewMode) {
        _libraryViewMode.value = mode
    }

    fun setLibrarySelectedType(type: String) {
        _librarySelectedType.value = type
    }

    fun setLibrarySortOrder(order: String) {
        _librarySortOrder.value = order
    }

    val libraryItems: StateFlow<List<MediaItem>> = combine(
        rawMediaItems,
        bookmarks,
        watchHistory
    ) { raw, bm, wh ->
        Triple(raw, bm, wh)
    }.combine(
        combine(_librarySegment, _librarySelectedType, _librarySortOrder) { seg, type, sort ->
            Triple(seg, type, sort)
        }
    ) { rawBmWh, segTypeSort ->
        filterAndSortLibraryItems(
            rawBmWh.first,
            rawBmWh.second,
            rawBmWh.third,
            segTypeSort.first,
            segTypeSort.second,
            segTypeSort.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val libraryGroupedItems: StateFlow<Map<String, List<MediaItem>>> = libraryItems.combine(
        _libraryViewMode
    ) { items, _ ->
        groupLibraryItemsByType(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val libraryAvailableTypes: StateFlow<List<String>> = combine(
        rawMediaItems,
        bookmarks,
        watchHistory,
        _librarySegment
    ) { raw, bm, wh, seg ->
        val base = filterAndSortLibraryItems(raw, bm, wh, seg, "All types", "Recently added")
        val types = base.map { it.category.ifBlank { "Other" } }.distinct()
        listOf("All types") + types
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        listOf("All types")
    )
}

enum class LibrarySegment {
    TERSIMPAN, CLOUD
}

enum class LibraryViewMode {
    ROW, GRID
}

fun filterAndSortLibraryItems(
    rawItems: List<MediaItem>,
    bookmarks: List<BookmarkEntity>,
    watchHistory: List<WatchHistoryEntity>,
    segment: LibrarySegment,
    selectedType: String,
    sortOrder: String
): List<MediaItem> {
    val baseItems = when (segment) {
        LibrarySegment.TERSIMPAN -> {
            val bookmarkIds = bookmarks.map { it.mediaId }.toSet()
            val historyIds = watchHistory.map { it.mediaId }.toSet()
            rawItems.filter { it.id in bookmarkIds || it.id in historyIds }
        }
        LibrarySegment.CLOUD -> {
            rawItems
        }
    }

    val filtered = if (selectedType == "All types" || selectedType.isBlank()) {
        baseItems
    } else {
        baseItems.filter { it.category.equals(selectedType, ignoreCase = true) }
    }

    val historyMap = watchHistory.associateBy { it.mediaId }

    return when (sortOrder) {
        "Title A-Z" -> filtered.sortedBy { it.title }
        "Title Z-A" -> filtered.sortedByDescending { it.title }
        "Year (terbaru)" -> filtered.sortedByDescending { it.releaseYear?.toIntOrNull() ?: 0 }
        "Recently watched" -> filtered.sortedByDescending { historyMap[it.id]?.updatedAt ?: 0L }
        else -> filtered
    }
}

fun groupLibraryItemsByType(items: List<MediaItem>): Map<String, List<MediaItem>> {
    return items.groupBy { it.category.ifBlank { "Other" } }
}

