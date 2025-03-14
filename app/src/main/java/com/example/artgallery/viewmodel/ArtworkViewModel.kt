package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.model.ArtworkFilter
import com.example.artgallery.repository.ArtworkRepository
import kotlinx.coroutines.launch

class ArtworkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ArtworkRepository
    val allArtworks: LiveData<List<Artwork>>
    val artworksForSale: LiveData<List<Artwork>>
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _artworks = MutableLiveData<List<Artwork>>()
    val artworks: LiveData<List<Artwork>> = _artworks

    private val _filteredArtworks = MediatorLiveData<List<Artwork>>()
    val filteredArtworks: LiveData<List<Artwork>> = _filteredArtworks

    private var currentFilter = ArtworkFilter()
    private val artworkList = mutableListOf<Artwork>()
    
    // Pagination variables
    private val PAGE_SIZE = 20
    private var currentPage = 0
    private var isLastPage = false

    init {
        val artworkDao = AppDatabase.getDatabase(application).artworkDao()
        repository = ArtworkRepository(artworkDao)
        allArtworks = repository.getAllArtworks()
        artworksForSale = repository.getArtworksForSale()
        
        _filteredArtworks.addSource(allArtworks) { artworks ->
            filterArtworks(artworks, currentFilter)
        }
        _filteredArtworks.addSource(_artworks) { artworks ->
            filterArtworks(artworks, currentFilter)
        }
    }

    fun refreshArtworks() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            repository.refreshArtworks()
            // Reset pagination
            currentPage = 0
            isLastPage = false
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to refresh artworks"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun loadNextPage() = viewModelScope.launch {
        if (isLastPage || _isLoading.value == true) return@launch
        
        _isLoading.value = true
        _error.value = null
        try {
            val nextPageArtworks = repository.getArtworksByPage(currentPage, PAGE_SIZE)
            if (nextPageArtworks.isEmpty()) {
                isLastPage = true
            } else {
                currentPage++
                val updatedList = artworkList.toMutableList()
                updatedList.addAll(nextPageArtworks)
                artworkList.clear()
                artworkList.addAll(updatedList)
                _artworks.value = artworkList.toList()
                applyFilter(currentFilter)
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load more artworks"
        } finally {
            _isLoading.value = false
        }
    }

    fun insertArtwork(artwork: Artwork) = viewModelScope.launch {
        _error.value = null
        try {
            repository.insertArtwork(artwork)
            artworkList.add(artwork)
            _artworks.value = artworkList.toList()
            applyFilter(currentFilter)
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to insert artwork"
        }
    }

    fun updateArtwork(artwork: Artwork) = viewModelScope.launch {
        _error.value = null
        try {
            val index = artworkList.indexOfFirst { it.id == artwork.id }
            if (index != -1) {
                artworkList[index] = artwork
                _artworks.value = artworkList.toList()
                applyFilter(currentFilter)
            }
            repository.updateArtwork(artwork)
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to update artwork"
        }
    }

    fun deleteArtwork(artwork: Artwork) = viewModelScope.launch {
        _error.value = null
        try {
            artworkList.remove(artwork)
            _artworks.value = artworkList.toList()
            applyFilter(currentFilter)
            repository.deleteArtwork(artwork)
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to delete artwork"
        }
    }

    fun applyFilter(filter: ArtworkFilter) {
        currentFilter = filter
        val filtered = artworkList.filter { artwork ->
            val matchesQuery = if (filter.query.isBlank()) {
                true
            } else {
                artwork.title.contains(filter.query, ignoreCase = true) ||
                artwork.description.contains(filter.query, ignoreCase = true)
            }

            val matchesCategory = if (filter.selectedCategories.isEmpty()) {
                true
            } else {
                filter.selectedCategories.contains(artwork.category)
            }

            matchesQuery && matchesCategory
        }
        _filteredArtworks.value = filtered
    }

    fun getArtworksByArtist(artistId: Long): LiveData<List<Artwork>> =
        repository.getArtworksByArtist(artistId)

    private fun filterArtworks(artworks: List<Artwork>, filter: ArtworkFilter) {
        if (filter == null) {
            _filteredArtworks.value = artworks
            return
        }

        _filteredArtworks.value = artworks.filter { artwork ->
            val matchesCategory = filter.selectedCategories.isEmpty() || 
                filter.selectedCategories.contains(artwork.category)
            
            val matchesSearch = filter.query.isBlank() || 
                artwork.title.contains(filter.query, ignoreCase = true) ||
                artwork.description.contains(filter.query, ignoreCase = true)
            
            matchesCategory && matchesSearch
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
