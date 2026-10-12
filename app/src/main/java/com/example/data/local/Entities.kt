package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val posterUrl: String,
    val streamUrl: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val category: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val category: String,
    val posterUrl: String,
    val streamUrl: String,
    val durationText: String,
    val rating: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_sources")
data class CustomSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val type: String, // "HLS", "MP4_DIRECT", "CATALOG_FEED"
    val isEnabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
