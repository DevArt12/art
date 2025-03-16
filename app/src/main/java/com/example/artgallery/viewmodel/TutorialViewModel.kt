package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.repository.TutorialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Art Classes & Tutorials functionality
 */
class TutorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TutorialRepository(application)
    
    // All tutorials
    private val _allTutorials = MutableLiveData<List<Tutorial>>(emptyList())
    val allTutorials: LiveData<List<Tutorial>> = _allTutorials
    
    // Current category filter
    private val _currentCategoryFilter = MutableLiveData<String?>(null)
    val currentCategoryFilter: LiveData<String?> = _currentCategoryFilter
    
    // Current difficulty filter
    private val _currentDifficultyFilter = MutableLiveData<String?>(null)
    val currentDifficultyFilter: LiveData<String?> = _currentDifficultyFilter
    
    // Current search query
    private val _currentSearchQuery = MutableLiveData<String?>(null)
    val currentSearchQuery: LiveData<String?> = _currentSearchQuery
    
    // Filtered tutorials based on category, difficulty, and search query
    val filteredTutorials: LiveData<List<Tutorial>> = _allTutorials.switchMap { tutorials ->
        _currentCategoryFilter.switchMap { category ->
            _currentDifficultyFilter.switchMap { difficulty ->
                _currentSearchQuery.map { query ->
                    var filtered = tutorials
                    
                    // Apply category filter
                    if (!category.isNullOrBlank()) {
                        filtered = filtered.filter { it.category == category }
                    }
                    
                    // Apply difficulty filter
                    if (!difficulty.isNullOrBlank()) {
                        filtered = filtered.filter { it.difficulty == difficulty }
                    }
                    
                    // Apply search filter
                    if (!query.isNullOrBlank()) {
                        filtered = filtered.filter { 
                            it.title.contains(query, ignoreCase = true) || 
                            it.description.contains(query, ignoreCase = true) 
                        }
                    }
                    
                    // Sort by date added (newest first)
                    filtered.sortedByDescending { it.dateAdded }
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
    
    init {
        // Load all tutorials
        refreshTutorials()
    }
    
    /**
     * Refresh the list of tutorials
     */
    fun refreshTutorials() {
        viewModelScope.launch {
            try {
                val tutorials = withContext(Dispatchers.IO) {
                    repository.getAllTutorialsSync()
                }
                _allTutorials.value = tutorials
            } catch (e: Exception) {
                _errorMessage.value = "Error loading tutorials: ${e.message}"
            }
        }
    }
    
    /**
     * Set the category filter
     */
    fun setTutorialCategoryFilter(category: String?) {
        _currentCategoryFilter.value = category
    }
    
    /**
     * Set the difficulty filter
     */
    fun setTutorialDifficultyFilter(difficulty: String?) {
        _currentDifficultyFilter.value = difficulty
    }
    
    /**
     * Search tutorials by query
     */
    fun searchTutorials(query: String) {
        _currentSearchQuery.value = query
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _currentSearchQuery.value = null
    }
    
    /**
     * Get tutorial by ID
     */
    fun getTutorialById(tutorialId: Long): LiveData<Tutorial?> {
        val result = MutableLiveData<Tutorial?>()
        viewModelScope.launch {
            try {
                val tutorial = withContext(Dispatchers.IO) {
                    repository.getTutorialById(tutorialId)
                }
                result.value = tutorial
            } catch (e: Exception) {
                _errorMessage.value = "Error getting tutorial: ${e.message}"
                result.value = null
            }
        }
        return result
    }
    
    /**
     * Download a tutorial
     */
    fun downloadTutorial(tutorialId: Long) {
        viewModelScope.launch {
            try {
                val tutorial = withContext(Dispatchers.IO) {
                    repository.getTutorialById(tutorialId)
                } ?: throw Exception("Tutorial not found")
                
                // Update download progress
                _downloadProgress.value = DownloadStatus(tutorialId, 0)
                
                // Simulate download progress (in a real app, this would be actual download logic)
                for (progress in 10..100 step 10) {
                    delay(300) // Simulate network delay
                    _downloadProgress.value = DownloadStatus(tutorialId, progress)
                }
                
                // Mark tutorial as downloaded
                val updatedTutorial = tutorial.copy(isDownloaded = true)
                withContext(Dispatchers.IO) {
                    repository.updateTutorial(updatedTutorial)
                }
                
                // Refresh tutorials to update UI
                refreshTutorials()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error downloading tutorial: ${e.message}"
            }
        }
    }
    
    /**
     * Delete a tutorial download
     */
    fun deleteTutorialDownload(tutorialId: Long) {
        viewModelScope.launch {
            try {
                val tutorial = withContext(Dispatchers.IO) {
                    repository.getTutorialById(tutorialId)
                } ?: throw Exception("Tutorial not found")
                
                // Delete the downloaded files
                withContext(Dispatchers.IO) {
                    repository.deleteTutorialFiles(tutorial)
                }
                
                // Mark tutorial as not downloaded
                val updatedTutorial = tutorial.copy(isDownloaded = false)
                withContext(Dispatchers.IO) {
                    repository.updateTutorial(updatedTutorial)
                }
                
                // Refresh tutorials to update UI
                refreshTutorials()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting tutorial download: ${e.message}"
            }
        }
    }
    
    /**
     * Increment view count for a tutorial
     */
    fun incrementViewCount(tutorialId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.incrementViewCount(tutorialId)
                }
            } catch (e: Exception) {
                // Don't show error for view count failures
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Update user progress for a tutorial
     */
    fun updateUserProgress(tutorialId: Long, progress: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateUserProgress(tutorialId, progress)
                }
                
                // Refresh tutorials to update UI
                refreshTutorials()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating progress: ${e.message}"
            }
        }
    }
    
    /**
     * Mark tutorial quiz as completed
     */
    fun markQuizCompleted(tutorialId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.markQuizCompleted(tutorialId)
                }
                
                // Refresh tutorials to update UI
                refreshTutorials()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating quiz status: ${e.message}"
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
     * Clear the download status
     */
    fun clearDownloadStatus() {
        _downloadProgress.value = null
    }
    
    /**
     * Format duration for display
     */
    fun formatDuration(durationInSeconds: Int): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }
    
    /**
     * Data class for download status
     */
    data class DownloadStatus(
        val tutorialId: Long,
        val progress: Int
    )
    
    /**
     * Helper function to delay execution
     */
    private suspend fun delay(timeMillis: Long) {
        kotlinx.coroutines.delay(timeMillis)
    }
}
