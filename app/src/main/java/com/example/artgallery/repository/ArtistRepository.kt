package com.example.artgallery.repository

import androidx.lifecycle.LiveData
import com.example.artgallery.data.dao.ArtistDao
import com.example.artgallery.data.entity.Artist

class ArtistRepository(private val artistDao: ArtistDao) {
    fun getAllArtists(): LiveData<List<Artist>> = artistDao.getAllArtists()

    suspend fun getArtistById(id: Long): Artist? = artistDao.getArtistById(id)

    suspend fun insertArtist(artist: Artist): Long = artistDao.insertArtist(artist)

    suspend fun updateArtist(artist: Artist) = artistDao.updateArtist(artist)

    suspend fun deleteArtist(artist: Artist) = artistDao.deleteArtist(artist)
}
