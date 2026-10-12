package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getHistory(mediaId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE mediaId = :mediaId")
    suspend fun deleteHistory(mediaId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY addedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId)")
    fun isBookmarked(mediaId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId")
    suspend fun deleteBookmark(mediaId: String)
}

@Dao
interface CustomSourceDao {
    @Query("SELECT * FROM custom_sources ORDER BY addedAt DESC")
    fun getAllSources(): Flow<List<CustomSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: CustomSourceEntity)

    @Query("UPDATE custom_sources SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleSource(id: String, isEnabled: Boolean)

    @Query("DELETE FROM custom_sources WHERE id = :id")
    suspend fun deleteSource(id: String)
}
