package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: Long): ProfileEntity?

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun getAllHistory(profileId: Long): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE mediaId = :mediaId AND profileId = :profileId LIMIT 1")
    suspend fun getHistory(mediaId: String, profileId: Long): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE mediaId = :mediaId AND profileId = :profileId")
    suspend fun deleteHistory(mediaId: String, profileId: Long)

    @Query("DELETE FROM watch_history WHERE profileId = :profileId")
    suspend fun clearHistoryForProfile(profileId: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun getAllBookmarks(profileId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId AND profileId = :profileId)")
    fun isBookmarked(mediaId: String, profileId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId AND profileId = :profileId")
    suspend fun deleteBookmark(mediaId: String, profileId: Long)

    @Query("DELETE FROM bookmarks WHERE profileId = :profileId")
    suspend fun deleteBookmarksForProfile(profileId: Long)
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
