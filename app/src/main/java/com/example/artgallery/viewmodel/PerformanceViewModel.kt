package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.entity.Performance
import com.example.artgallery.repository.PerformanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel for Performance Hub functionality
 */
class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PerformanceRepository(application)
    
    // All performances
    private val _allPerformances = MutableLiveData<List<Performance>>(emptyList())
    val allPerformances: LiveData<List<Performance>> = _allPerformances
    
    // Currently selected performance
    private val _selectedPerformance = MutableLiveData<Performance?>()
    val selectedPerformance: LiveData<Performance?> = _selectedPerformance
    
    // Current category filter
    private val _currentCategoryFilter = MutableLiveData<String?>(null)
    val currentCategoryFilter: LiveData<String?> = _currentCategoryFilter
    
    // Filtered performances based on category
    val filteredPerformances: LiveData<List<Performance>> = _currentCategoryFilter.switchMap { category ->
        when (category) {
            null -> allPerformances // No filter, show all performances
            "downloaded" -> {
                // Filter to only show downloaded performances
                _allPerformances.map { performances ->
                    performances.filter { it.isDownloaded }
                }
            }
            else -> {
                // Filter by category
                _allPerformances.map { performances ->
                    performances.filter { it.category == category }
                }
            }
        }
    }
    
    // Download progress
    private val _downloadProgress = MutableLiveData<DownloadStatus>()
    val downloadProgress: LiveData<DownloadStatus> = _downloadProgress
    
    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Playback position for current performance
    private val _currentPlaybackPosition = MutableLiveData<Int>(0)
    val currentPlaybackPosition: LiveData<Int> = _currentPlaybackPosition
    
    // Is performance playing
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    
    init {
        // Load all performances
        refreshPerformances()
    }
    
    /**
     * Refresh the list of performances
     */
    fun refreshPerformances() {
        viewModelScope.launch {
            try {
                val performances = withContext(Dispatchers.IO) {
                    repository.getAllPerformancesSync()
                }
                _allPerformances.value = performances
            } catch (e: Exception) {
                _errorMessage.value = "Error loading performances: ${e.message}"
            }
        }
    }
    
    /**
     * Set the category filter
     */
    fun setPerformanceCategoryFilter(category: String?) {
        _currentCategoryFilter.value = category
    }
    
    /**
     * Select a performance by ID
     */
    fun selectPerformance(performanceId: Long) {
        viewModelScope.launch {
            try {
                val performance = withContext(Dispatchers.IO) {
                    repository.getPerformanceById(performanceId)
                }
                _selectedPerformance.value = performance
            } catch (e: Exception) {
                _errorMessage.value = "Error selecting performance: ${e.message}"
            }
        }
    }
    
    /**
     * Get a performance by ID
     */
    fun getPerformanceById(performanceId: Long): LiveData<Performance?> {
        val result = MutableLiveData<Performance?>()
        viewModelScope.launch {
            try {
                val performance = withContext(Dispatchers.IO) {
                    repository.getPerformanceById(performanceId)
                }
                result.value = performance
            } catch (e: Exception) {
                _errorMessage.value = "Error getting performance: ${e.message}"
                result.value = null
            }
        }
        return result
    }
    
    /**
     * Download a performance
     */
    fun downloadPerformance(performanceId: Long) {
        viewModelScope.launch {
            try {
                // Check if performance exists
                val performance = withContext(Dispatchers.IO) {
                    repository.getPerformanceById(performanceId)
                } ?: throw Exception("Performance not found")
                
                // Start download
                _downloadProgress.value = DownloadStatus.InProgress(performanceId, 0)
                
                // Simulate download progress (in a real app, this would be actual download logic)
                for (progress in 10..100 step 10) {
                    _downloadProgress.value = DownloadStatus.InProgress(performanceId, progress)
                    delay(500) // Simulate network delay
                }
                
                // Mark performance as downloaded
                withContext(Dispatchers.IO) {
                    repository.markPerformanceAsDownloaded(performanceId)
                }
                
                // Update download status
                _downloadProgress.value = DownloadStatus.Success(performanceId)
                
                // Refresh performances to update UI
                refreshPerformances()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error downloading performance: ${e.message}"
                _downloadProgress.value = DownloadStatus.Failed(performanceId, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Update playback position
     */
    fun updatePlaybackPosition(position: Int) {
        _currentPlaybackPosition.value = position
    }
    
    /**
     * Set playing state
     */
    fun setPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
    
    /**
     * Increment view count for a performance
     */
    fun incrementViewCount(performanceId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.incrementViewCount(performanceId)
            }
        }
    }
    
    /**
     * Clear the error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Format duration for display
     */
    fun formatDuration(durationInSeconds: Int): String {
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    /**
     * Download status
     */
    sealed class DownloadStatus {
        data class InProgress(val performanceId: Long, val progress: Int) : DownloadStatus()
        data class Success(val performanceId: Long) : DownloadStatus()
        data class Failed(val performanceId: Long, val error: String) : DownloadStatus()
    }
    
    // Helper function to simulate delay
    private suspend fun delay(timeMillis: Long) {
        withContext(Dispatchers.IO) {
            Thread.sleep(timeMillis)
        }
    }
}
