package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        BookmarkEntity::class,
        CustomSourceEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun customSourceDao(): CustomSourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Hapus entri riwayat tonton dan bookmark yang berasal dari demo catalog ("cs-...")
                db.execSQL("DELETE FROM watch_history WHERE mediaId LIKE 'cs-%'")
                db.execSQL("DELETE FROM bookmarks WHERE mediaId LIKE 'cs-%'")

                // 2. Hapus custom sources bawaan/demo yang mengarah ke domain testing/sample
                db.execSQL(
                    """
                    DELETE FROM custom_sources 
                    WHERE url LIKE '%commondatastorage.googleapis.com%'
                       OR url LIKE '%images.unsplash.com%'
                       OR url LIKE '%devstreaming-cdn.apple.com%'
                       OR url LIKE '%demo.unified-streaming.com%'
                       OR url LIKE '%test-streams.mux.dev%'
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cloudstream_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
