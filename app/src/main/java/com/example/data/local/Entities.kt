package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val avatarId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history", primaryKeys = ["mediaId", "profileId"])
data class WatchHistoryEntity(
    val mediaId: String,
    val profileId: Long = 1L,
    val title: String,
    val posterUrl: String,
    val streamUrl: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val category: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks", primaryKeys = ["mediaId", "profileId"])
data class BookmarkEntity(
    val mediaId: String,
    val profileId: Long = 1L,
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
