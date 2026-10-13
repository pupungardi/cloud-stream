package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.model.MediaItem
import com.example.model.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.UUID

class MediaRepository(private val database: AppDatabase, private val profileRepository: ProfileRepository? = null) {

    private val watchHistoryDao = database.watchHistoryDao()
    private val bookmarkDao = database.bookmarkDao()
    private val customSourceDao = database.customSourceDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeProfileIdFlow: Flow<Long> = profileRepository?.activeProfileId ?: flowOf(1L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val watchHistory: Flow<List<WatchHistoryEntity>> = activeProfileIdFlow
        .flatMapLatest { profileId ->
            watchHistoryDao.getAllHistory(profileId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarks: Flow<List<BookmarkEntity>> = activeProfileIdFlow
        .flatMapLatest { profileId ->
            bookmarkDao.getAllBookmarks(profileId)
        }

    val customSources: Flow<List<CustomSourceEntity>> = customSourceDao.getAllSources()

    private fun getActiveProfileId(): Long {
        return profileRepository?.activeProfileId?.value ?: 1L
    }

    fun getAllMediaItems(): Flow<List<MediaItem>> {
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
                        description = source.url,
                        category = categoryName,
                        posterUrl = null,
                        backdropUrl = null,
                        durationText = if (isHls) "LIVE" else "Stream",
                        durationMs = 0L,
                        releaseYear = null,
                        rating = null,
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
        val profileId = getActiveProfileId()
        return watchHistoryDao.getHistory(mediaId, profileId)
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
        val profileId = getActiveProfileId()
        val entity = WatchHistoryEntity(
            mediaId = mediaId,
            profileId = profileId,
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
        val profileId = getActiveProfileId()
        watchHistoryDao.deleteHistory(mediaId, profileId)
    }

    suspend fun clearWatchHistory() {
        val profileId = getActiveProfileId()
        watchHistoryDao.clearHistoryForProfile(profileId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isBookmarked(mediaId: String): Flow<Boolean> {
        return activeProfileIdFlow.flatMapLatest { profileId ->
            bookmarkDao.isBookmarked(mediaId, profileId)
        }
    }

    suspend fun toggleBookmark(mediaItem: MediaItem, isCurrentlyBookmarked: Boolean) {
        val profileId = getActiveProfileId()
        if (isCurrentlyBookmarked) {
            bookmarkDao.deleteBookmark(mediaItem.id, profileId)
        } else {
            val entity = BookmarkEntity(
                mediaId = mediaItem.id,
                profileId = profileId,
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
