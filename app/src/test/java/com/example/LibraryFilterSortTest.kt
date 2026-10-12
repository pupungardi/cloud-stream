package com.example

import com.example.data.local.BookmarkEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.ui.viewmodel.LibrarySegment
import com.example.ui.viewmodel.filterAndSortLibraryItems
import com.example.ui.viewmodel.groupLibraryItemsByType
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryFilterSortTest {

    private val sampleItems = listOf(
        MediaItem(id = "1", title = "Zebra Movie", category = "Movie", releaseYear = "2020", streamSources = emptyList()),
        MediaItem(id = "2", title = "Alpha Movie", category = "Movie", releaseYear = "2023", streamSources = emptyList()),
        MediaItem(id = "3", title = "Beta Series", category = "Series", releaseYear = "2022", streamSources = emptyList())
    )

    private val sampleBookmarks = listOf(
        BookmarkEntity(mediaId = "1", title = "Zebra Movie", category = "Movie", posterUrl = "", streamUrl = "", durationText = "120m", rating = 4.5f, addedAt = 100L),
        BookmarkEntity(mediaId = "2", title = "Alpha Movie", category = "Movie", posterUrl = "", streamUrl = "", durationText = "110m", rating = 4.0f, addedAt = 200L)
    )

    private val sampleHistory = listOf(
        WatchHistoryEntity(mediaId = "3", title = "Beta Series", posterUrl = "", streamUrl = "", lastPositionMs = 0L, durationMs = 100L, updatedAt = 300L)
    )

    @Test
    fun testFilterAndSortTersimpanSegment() {
        val filtered = filterAndSortLibraryItems(
            rawItems = sampleItems,
            bookmarks = sampleBookmarks,
            watchHistory = sampleHistory,
            segment = LibrarySegment.TERSIMPAN,
            selectedType = "All types",
            sortOrder = "Title A-Z"
        )
        assertEquals(3, filtered.size)
        assertEquals("Alpha Movie", filtered[0].title)
        assertEquals("Beta Series", filtered[1].title)
        assertEquals("Zebra Movie", filtered[2].title)
    }

    @Test
    fun testSortByYear() {
        val sorted = filterAndSortLibraryItems(
            rawItems = sampleItems,
            bookmarks = sampleBookmarks,
            watchHistory = sampleHistory,
            segment = LibrarySegment.CLOUD,
            selectedType = "All types",
            sortOrder = "Year (terbaru)"
        )
        assertEquals("2023", sorted[0].releaseYear)
        assertEquals("2022", sorted[1].releaseYear)
        assertEquals("2020", sorted[2].releaseYear)
    }

    @Test
    fun testGroupByType() {
        val grouped = groupLibraryItemsByType(sampleItems)
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["Movie"]?.size)
        assertEquals(1, grouped["Series"]?.size)
    }
}
