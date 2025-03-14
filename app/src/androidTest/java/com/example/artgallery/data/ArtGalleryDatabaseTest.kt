package com.example.artgallery.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.artgallery.data.dao.ArtistDao
import com.example.artgallery.data.dao.ArtworkDao
import com.example.artgallery.data.entity.Artist
import com.example.artgallery.data.entity.Artwork
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ArtGalleryDatabaseTest {
    private lateinit var artistDao: ArtistDao
    private lateinit var artworkDao: ArtworkDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        artistDao = db.artistDao()
        artworkDao = db.artworkDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetArtist() = runBlocking {
        val artist = Artist(
            name = "Test Artist",
            bio = "Test Bio",
            profilePicturePath = "test/path"
        )
        val artistId = artistDao.insertArtist(artist)
        val retrievedArtist = artistDao.getArtistById(artistId)
        assert(retrievedArtist?.name == artist.name)
        assert(retrievedArtist?.bio == artist.bio)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetArtwork() = runBlocking {
        val artist = Artist(
            name = "Test Artist",
            bio = "Test Bio",
            profilePicturePath = "test/path"
        )
        val artistId = artistDao.insertArtist(artist)

        val artwork = Artwork(
            title = "Test Artwork",
            description = "Test Description",
            imagePath = "test/path",
            artistId = artistId,
            isForSale = true,
            price = 100.0,
            contactDetails = "test@email.com"
        )
        artworkDao.insertArtwork(artwork)

        val artworks = artworkDao.getArtworksByArtist(artistId)
        assert(artworks.value?.isNotEmpty() == true)
    }
}
