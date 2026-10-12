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

    private val _selectedCategory = MutableStateFlow("All")
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

            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)

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
        _selectedCategory.value = category
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
}
