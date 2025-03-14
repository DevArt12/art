package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.Artist
import com.example.artgallery.repository.ArtistRepository
import kotlinx.coroutines.launch

class ArtistViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ArtistRepository
    val allArtists: LiveData<List<Artist>>

    init {
        val artistDao = AppDatabase.getDatabase(application).artistDao()
        repository = ArtistRepository(artistDao)
        allArtists = repository.getAllArtists()
    }

    fun insertArtist(artist: Artist) = viewModelScope.launch {
        repository.insertArtist(artist)
    }

    fun updateArtist(artist: Artist) = viewModelScope.launch {
        repository.updateArtist(artist)
    }

    fun deleteArtist(artist: Artist) = viewModelScope.launch {
        repository.deleteArtist(artist)
    }

    suspend fun getArtistById(id: Long): Artist? = repository.getArtistById(id)
}
