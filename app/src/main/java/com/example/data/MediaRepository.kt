package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.model.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class MediaRepository(private val database: AppDatabase) {

    private val watchHistoryDao = database.watchHistoryDao()
    private val bookmarkDao = database.bookmarkDao()
    private val customSourceDao = database.customSourceDao()

    val watchHistory: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getAllHistory()
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val customSources: Flow<List<CustomSourceEntity>> = customSourceDao.getAllSources()

    fun getAllMediaItems(): Flow<List<MediaItem>> {
        return customSources.combine(watchHistory) { sources, _ ->
            val allList = CuratedMediaCatalog.items.toMutableList()

            // Convert active custom sources into playable media items
            sources.filter { it.isEnabled }.forEach { source ->
                val sourceId = "custom-${source.id}"
                val isHls = source.url.endsWith(".m3u8", ignoreCase = true)
                allList.add(
                    MediaItem(
                        id = sourceId,
                        title = source.name,
                        description = "Imported custom stream from repository source: ${source.url}",
                        category = if (isHls) "Space & Live" else "Custom Stream",
                        posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                        backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
                        durationText = if (isHls) "LIVE" else "Stream",
                        durationMs = 0L,
                        releaseYear = "Custom",
                        rating = 9.0f,
                        isLive = isHls,
                        streamSources = listOf(
                            StreamSource(
                                label = if (isHls) "HLS Live Stream" else "Direct Video Feed",
                                url = source.url,
                                quality = "Auto"
                            )
                        ),
                        sourceRepo = source.name
                    )
                )
            }
            allList
        }
    }

    suspend fun getWatchHistoryItem(mediaId: String): WatchHistoryEntity? {
        return watchHistoryDao.getHistory(mediaId)
    }

    suspend fun saveWatchProgress(
        mediaId: String,
        title: String,
        posterUrl: String,
        streamUrl: String,
        lastPositionMs: Long,
        durationMs: Long,
        category: String = ""
    ) {
        val entity = WatchHistoryEntity(
            mediaId = mediaId,
            title = title,
            posterUrl = posterUrl,
            streamUrl = streamUrl,
            lastPositionMs = lastPositionMs,
            durationMs = durationMs,
            category = category,
            updatedAt = System.currentTimeMillis()
        )
        watchHistoryDao.upsertHistory(entity)
    }

    suspend fun deleteWatchHistory(mediaId: String) {
        watchHistoryDao.deleteHistory(mediaId)
    }

    suspend fun clearWatchHistory() {
        watchHistoryDao.clearAllHistory()
    }

    fun isBookmarked(mediaId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(mediaId)
    }

    suspend fun toggleBookmark(mediaItem: MediaItem, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            bookmarkDao.deleteBookmark(mediaItem.id)
        } else {
            val entity = BookmarkEntity(
                mediaId = mediaItem.id,
                title = mediaItem.title,
                category = mediaItem.category,
                posterUrl = mediaItem.posterUrl,
                streamUrl = mediaItem.streamSources.firstOrNull()?.url ?: "",
                durationText = mediaItem.durationText,
                rating = mediaItem.rating,
                addedAt = System.currentTimeMillis()
            )
            bookmarkDao.insertBookmark(entity)
        }
    }

    suspend fun addCustomSource(name: String, url: String, type: String = "DIRECT_STREAM") {
        val entity = CustomSourceEntity(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Source" },
            url = url.trim(),
            type = type,
            isEnabled = true,
            addedAt = System.currentTimeMillis()
        )
        customSourceDao.insertSource(entity)
    }

    suspend fun toggleCustomSource(id: String, isEnabled: Boolean) {
        customSourceDao.toggleSource(id, isEnabled)
    }

    suspend fun deleteCustomSource(id: String) {
        customSourceDao.deleteSource(id)
    }

    suspend fun testUrlConnectivity(streamUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(streamUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.instanceFollowRedirects = true
            val responseCode = connection.responseCode
            responseCode in 200..399
        } catch (_: Exception) {
            // Some streaming CDNs reject HEAD requests; fallback to GET with short range
            try {
                val url = URL(streamUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Range", "bytes=0-1024")
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                val code = connection.responseCode
                code in 200..399
            } catch (_: Exception) {
                false
            }
        }
    }
}
