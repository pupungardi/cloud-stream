package com.example

import com.example.data.MediaRepository
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomSourceDao
import com.example.data.local.CustomSourceEntity
import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakeCustomSourceDao(
    private val flow: MutableStateFlow<List<CustomSourceEntity>> = MutableStateFlow(emptyList())
) : CustomSourceDao {
    override fun getAllSources(): Flow<List<CustomSourceEntity>> = flow
    override suspend fun insertSource(source: CustomSourceEntity) {
        flow.value = flow.value + source
    }
    override suspend fun toggleSource(id: String, isEnabled: Boolean) {
        flow.value = flow.value.map { if (it.id == id) it.copy(isEnabled = isEnabled) else it }
    }
    override suspend fun deleteSource(id: String) {
        flow.value = flow.value.filter { it.id != id }
    }
}

class FakeWatchHistoryDao : WatchHistoryDao {
    private val flow = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())
    override fun getAllHistory(): Flow<List<WatchHistoryEntity>> = flow
    override suspend fun getHistory(mediaId: String): WatchHistoryEntity? = null
    override suspend fun upsertHistory(history: WatchHistoryEntity) {}
    override suspend fun deleteHistory(mediaId: String) {}
    override suspend fun clearAllHistory() {}
}

class FakeBookmarkDao : BookmarkDao {
    private val flow = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    override fun getAllBookmarks(): Flow<List<BookmarkEntity>> = flow
    override fun isBookmarked(mediaId: String): Flow<Boolean> = flowOf(false)
    override suspend fun insertBookmark(bookmark: BookmarkEntity) {}
    override suspend fun deleteBookmark(mediaId: String) {}
}

class FakeAppDatabase(
    private val customSourceDaoInstance: CustomSourceDao = FakeCustomSourceDao(),
    private val watchHistoryDaoInstance: WatchHistoryDao = FakeWatchHistoryDao(),
    private val bookmarkDaoInstance: BookmarkDao = FakeBookmarkDao()
) : AppDatabase() {
    override fun watchHistoryDao(): WatchHistoryDao = watchHistoryDaoInstance
    override fun bookmarkDao(): BookmarkDao = bookmarkDaoInstance
    override fun customSourceDao(): CustomSourceDao = customSourceDaoInstance
    override fun clearAllTables() {}
    @Suppress("DEPRECATION")
    override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
        return androidx.room.InvalidationTracker(this, "watch_history", "bookmarks", "custom_sources")
    }
    override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): androidx.sqlite.db.SupportSQLiteOpenHelper {
        throw NotImplementedError()
    }
}

class NoDemoDataGuardTest {

    @Test
    fun `getAllMediaItems returns empty list when custom sources are empty`() = runBlocking {
        val fakeDao = FakeCustomSourceDao(MutableStateFlow(emptyList()))
        val fakeDb = FakeAppDatabase(customSourceDaoInstance = fakeDao)

        val repository = MediaRepository(fakeDb)
        val items = repository.getAllMediaItems().first()

        assertTrue("Katalog harus kosong saat database tidak memiliki custom sources", items.isEmpty())
    }

    @Test
    fun `getAllMediaItems ignores disabled custom sources`() = runBlocking {
        val disabledSource = CustomSourceEntity(
            id = "user-source-1",
            name = "My Personal Server",
            url = "https://myserver.example.com/playlist.m3u8",
            type = "DIRECT_STREAM",
            isEnabled = false
        )
        val fakeDao = FakeCustomSourceDao(MutableStateFlow(listOf(disabledSource)))
        val fakeDb = FakeAppDatabase(customSourceDaoInstance = fakeDao)

        val repository = MediaRepository(fakeDb)
        val items = repository.getAllMediaItems().first()

        assertTrue("Sumber yang di-disable tidak boleh muncul di katalog", items.isEmpty())
    }

    @Test
    fun `getAllMediaItems extracts media correctly from enabled user source without fake data`() = runBlocking {
        val enabledSource = CustomSourceEntity(
            id = "user-source-2",
            name = "Independent Cinema Feed",
            url = "https://indie.example.com/live/stream.m3u8",
            type = "DIRECT_STREAM",
            isEnabled = true
        )
        val fakeDao = FakeCustomSourceDao(MutableStateFlow(listOf(enabledSource)))
        val fakeDb = FakeAppDatabase(customSourceDaoInstance = fakeDao)

        val repository = MediaRepository(fakeDb)
        val items = repository.getAllMediaItems().first()

        assertEquals(1, items.size)
        val item = items[0]
        assertEquals("Independent Cinema Feed", item.title)
        assertEquals(null, item.posterUrl)
        assertEquals(null, item.backdropUrl)
        assertEquals(null, item.rating)
        assertEquals(null, item.releaseYear)
        assertEquals("https://indie.example.com/live/stream.m3u8", item.description)
    }

    @Test
    fun `source code in app main must not contain demo domains or sample movie titles`() {
        val forbiddenKeywords = listOf(
            "commondatastorage.googleapis.com",
            "images.unsplash.com",
            "devstreaming-cdn.apple.com",
            "demo.unified-streaming.com",
            "test-streams.mux.dev",
            "Sintel",
            "Big Buck Bunny",
            "Tears of Steel",
            "Elephants Dream"
        )

        // Cari folder src/main/java
        var srcDir = File("src/main/java/com/example")
        if (!srcDir.exists()) {
            srcDir = File("app/src/main/java/com/example")
        }
        assertTrue("Direktori source code com/example harus ditemukan", srcDir.exists())

        val violations = mutableListOf<String>()

        srcDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            // Kecualikan AppDatabase.kt karena query SQL migration 1->2 sengaja membersihkan domain demo tersebut
            .filter { it.name != "AppDatabase.kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    forbiddenKeywords.forEach { keyword ->
                        if (line.contains(keyword, ignoreCase = false)) {
                            violations.add("${file.name}:${index + 1} contains '$keyword' -> $line")
                        }
                    }
                }
            }

        assertTrue(
            "Pagar pengaman gagal! Ditemukan data demo di app/src/main:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }
}
