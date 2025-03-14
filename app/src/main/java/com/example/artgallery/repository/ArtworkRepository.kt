package com.example.artgallery.repository

import androidx.lifecycle.LiveData
import com.example.artgallery.data.dao.ArtworkDao
import com.example.artgallery.data.entity.Artwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

class ArtworkRepository(private val artworkDao: ArtworkDao) {
    fun getAllArtworks(): LiveData<List<Artwork>> = artworkDao.getAllArtworks()

    fun getArtworksForSale(): LiveData<List<Artwork>> = artworkDao.getArtworksForSale()

    fun getArtworksByArtist(artistId: Long): LiveData<List<Artwork>> = 
        artworkDao.getArtworksByArtist(artistId)

    suspend fun getArtworksByPage(page: Int, pageSize: Int): List<Artwork> = withContext(Dispatchers.IO) {
        try {
            val offset = page * pageSize
            return@withContext artworkDao.getArtworksByPage(pageSize, offset)
        } catch (e: Exception) {
            throw IOException("Failed to load artworks: ${e.message}", e)
        }
    }

    suspend fun insertArtwork(artwork: Artwork): Long = withContext(Dispatchers.IO) {
        try {
            return@withContext artworkDao.insertArtwork(artwork)
        } catch (e: Exception) {
            throw IOException("Failed to insert artwork: ${e.message}", e)
        }
    }

    suspend fun updateArtwork(artwork: Artwork) = withContext(Dispatchers.IO) {
        try {
            artworkDao.updateArtwork(artwork)
        } catch (e: Exception) {
            throw IOException("Failed to update artwork: ${e.message}", e)
        }
    }

    suspend fun deleteArtwork(artwork: Artwork) = withContext(Dispatchers.IO) {
        try {
            artworkDao.deleteArtwork(artwork)
        } catch (e: Exception) {
            throw IOException("Failed to delete artwork: ${e.message}", e)
        }
    }
    
    suspend fun refreshArtworks() = withContext(Dispatchers.IO) {
        try {
            // In a real app, this would fetch from a remote data source
            // For now, we'll just simulate a delay
            delay(1000)
            
            // Simulate network errors occasionally (for testing purposes)
            if (Math.random() < 0.1) {
                throw IOException("Network error occurred")
            }
        } catch (e: Exception) {
            throw IOException("Failed to refresh artworks: ${e.message}", e)
        }
    }
    
    suspend fun searchArtworks(query: String): List<Artwork> = withContext(Dispatchers.IO) {
        try {
            return@withContext artworkDao.searchArtworks("%$query%")
        } catch (e: Exception) {
            throw IOException("Failed to search artworks: ${e.message}", e)
        }
    }
}
