package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.model.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
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
        // Katalog HANYA berasal dari custom source yang isEnabled di database.
        // Tidak ada daftar bawaan, tidak ada seed saat first launch.
        return customSources.map { sources ->
            val allList = mutableListOf<MediaItem>()

            sources.filter { it.isEnabled }.forEach { source ->
                val sourceId = "custom-${source.id}"
                val isHls = isHlsUrl(source.url)
                val categoryName = if (isHls) "HLS Stream" else "Direct Video"

                allList.add(
                    MediaItem(
                        id = sourceId,
                        title = source.name,
                        description = source.url, // Hanya tampilkan URL sumber atau kosongkan
                        category = categoryName,
                        posterUrl = null, // Tidak memakai poster Unsplash palsu
                        backdropUrl = null,
                        durationText = if (isHls) "LIVE" else "Stream",
                        durationMs = 0L,
                        releaseYear = null, // Tidak memakai nilai palsu "Custom"
                        rating = null,      // Tidak memakai nilai palsu 9.0f
                        isLive = isHls,
                        streamSources = listOf(
                            StreamSource(
                                label = if (isHls) "HLS Live Stream" else "Direct Stream",
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

    private fun isHlsUrl(rawUrl: String): Boolean {
        return try {
            val uri = URI(rawUrl)
            val path = uri.path ?: ""
            path.endsWith(".m3u8", ignoreCase = true)
        } catch (_: Exception) {
            rawUrl.substringBefore('?').substringBefore('#').endsWith(".m3u8", ignoreCase = true)
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
                posterUrl = mediaItem.posterUrl ?: "",
                streamUrl = mediaItem.streamSources.firstOrNull()?.url ?: "",
                durationText = mediaItem.durationText,
                rating = mediaItem.rating ?: 0f,
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
        // Percobaan 1: Menggunakan HEAD request dengan penanganan redirect
        var currentUrl = streamUrl
        var redirects = 0
        while (redirects < 5) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = 4000
                    readTimeout = 4000
                    instanceFollowRedirects = false
                }
                val code = connection.responseCode
                if (code in 200..299 || code == 206) {
                    return@withContext true
                } else if (code in 300..399) {
                    val location = connection.getHeaderField("Location") ?: break
                    currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                        location
                    } else {
                        URL(url, location).toString()
                    }
                    redirects++
                } else {
                    break
                }
            } catch (_: Exception) {
                break
            } finally {
                connection?.disconnect()
            }
        }

        // Percobaan 2 (Fallback): Beberapa server media menolak HEAD; coba GET dengan byte-range
        currentUrl = streamUrl
        redirects = 0
        while (redirects < 5) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Range", "bytes=0-1024")
                    connectTimeout = 4000
                    readTimeout = 4000
                    instanceFollowRedirects = false
                }
                val code = connection.responseCode
                if (code in 200..299 || code == 206) {
                    return@withContext true
                } else if (code in 300..399) {
                    val location = connection.getHeaderField("Location") ?: return@withContext false
                    currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                        location
                    } else {
                        URL(url, location).toString()
                    }
                    redirects++
                } else {
                    return@withContext false
                }
            } catch (_: Exception) {
                return@withContext false
            } finally {
                connection?.disconnect()
            }
        }

        false
    }
}
