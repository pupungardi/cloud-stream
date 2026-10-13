package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProfileEntity::class,
        WatchHistoryEntity::class,
        BookmarkEntity::class,
        CustomSourceEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun customSourceDao(): CustomSourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM watch_history WHERE mediaId LIKE 'cs-%'")
                db.execSQL("DELETE FROM bookmarks WHERE mediaId LIKE 'cs-%'")
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create profiles table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `avatarId` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Insert default profile "Profil 1"
                db.execSQL("""
                    INSERT INTO `profiles` (`name`, `avatarId`, `createdAt`) 
                    VALUES ('Profil 1', 'avatar_1', ${System.currentTimeMillis()})
                """.trimIndent())

                // 3. Recreate watch_history with composite primary key (mediaId, profileId)
                db.execSQL("CREATE TABLE IF NOT EXISTS `watch_history_new` (`mediaId` TEXT NOT NULL, `profileId` INTEGER NOT NULL, `title` TEXT NOT NULL, `posterUrl` TEXT NOT NULL, `streamUrl` TEXT NOT NULL, `lastPositionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `category` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`mediaId`, `profileId`))")
                db.execSQL("INSERT INTO `watch_history_new` (`mediaId`, `profileId`, `title`, `posterUrl`, `streamUrl`, `lastPositionMs`, `durationMs`, `category`, `updatedAt`) SELECT `mediaId`, 1, `title`, `posterUrl`, `streamUrl`, `lastPositionMs`, `durationMs`, `category`, `updatedAt` FROM `watch_history`")
                db.execSQL("DROP TABLE `watch_history`")
                db.execSQL("ALTER TABLE `watch_history_new` RENAME TO `watch_history`")

                // 4. Recreate bookmarks with composite primary key (mediaId, profileId)
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks_new` (`mediaId` TEXT NOT NULL, `profileId` INTEGER NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `posterUrl` TEXT NOT NULL, `streamUrl` TEXT NOT NULL, `durationText` TEXT NOT NULL, `rating` REAL NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`mediaId`, `profileId`))")
                db.execSQL("INSERT INTO `bookmarks_new` (`mediaId`, `profileId`, `title`, `category`, `posterUrl`, `streamUrl`, `durationText`, `rating`, `addedAt`) SELECT `mediaId`, 1, `title`, `category`, `posterUrl`, `streamUrl`, `durationText`, `rating`, `addedAt` FROM `bookmarks`")
                db.execSQL("DROP TABLE `bookmarks`")
                db.execSQL("ALTER TABLE `bookmarks_new` RENAME TO `bookmarks`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cloudstream_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
