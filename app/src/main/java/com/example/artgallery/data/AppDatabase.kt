package com.example.artgallery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.artgallery.data.dao.*
import com.example.artgallery.data.entity.*

@Database(
    entities = [
        Artist::class, 
        Artwork::class, 
        ArtNews::class,
        Performance::class,
        Tutorial::class,
        Event::class,
        ForumPost::class,
        ForumComment::class,
        ARModel::class,
        MarketItem::class //Added MarketItem entity
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun artworkDao(): ArtworkDao
    abstract fun artNewsDao(): ArtNewsDao
    abstract fun performanceDao(): PerformanceDao
    abstract fun tutorialDao(): TutorialDao
    abstract fun eventDao(): EventDao
    abstract fun forumPostDao(): ForumPostDao
    abstract fun forumCommentDao(): ForumCommentDao
    abstract fun arModelDao(): ARModelDao
    abstract fun marketDao(): MarketDao //Added MarketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new tables for Performance, Tutorial, Event, Forum, and AR features
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS performances (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        venue TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        ticketPrice REAL,
                        imagePath TEXT
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tutorials (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        thumbnailPath TEXT,
                        videoUrl TEXT
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        location TEXT NOT NULL,
                        capacity INTEGER,
                        registrationUrl TEXT,
                        imagePath TEXT
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS forum_posts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        authorId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        imagePath TEXT
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS forum_comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        postId INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        authorId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(postId) REFERENCES forum_posts(id) ON DELETE CASCADE
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ar_models (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        modelPath TEXT NOT NULL,
                        thumbnailPath TEXT,
                        fileSize INTEGER NOT NULL,
                        downloadUrl TEXT,
                        isDownloaded INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS market_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        price REAL NOT NULL,
                        imageUrl TEXT NOT NULL,
                        artistId INTEGER NOT NULL,
                        FOREIGN KEY(artistId) REFERENCES artists(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "art_gallery_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}