package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.entity.ARModel
import com.example.artgallery.repository.ARModelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel for AR Studio functionality
 */
class ARStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ARModelRepository(application)
    
    // All AR models
    private val _allModels = MutableLiveData<List<ARModel>>(emptyList())
    val allModels: LiveData<List<ARModel>> = _allModels
    
    // Currently selected model
    private val _selectedModel = MutableLiveData<ARModel?>()
    val selectedModel: LiveData<ARModel?> = _selectedModel
    
    // Current category filter
    private val _currentCategoryFilter = MutableLiveData<String?>(null)
    val currentCategoryFilter: LiveData<String?> = _currentCategoryFilter
    
    // Filtered models based on category
    val filteredModels: LiveData<List<ARModel>> = _currentCategoryFilter.switchMap { category ->
        when (category) {
            null -> allModels // No filter, show all models
            "downloaded" -> {
                // Filter to only show downloaded models
                _allModels.map { models ->
                    models.filter { it.isDownloaded }
                }
            }
            else -> {
                // Filter by category
                _allModels.map { models ->
                    models.filter { it.category == category }
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
    
    // Model placement status
    private val _isModelPlaced = MutableLiveData<Boolean>(false)
    val isModelPlaced: LiveData<Boolean> = _isModelPlaced
    
    // AR tracking status
    private val _trackingStatus = MutableLiveData<TrackingStatus>(TrackingStatus.NOT_TRACKING)
    val trackingStatus: LiveData<TrackingStatus> = _trackingStatus
    
    init {
        // Load all models
        refreshModels()
    }
    
    /**
     * Refresh the list of AR models
     */
    fun refreshModels() {
        viewModelScope.launch {
            try {
                val models = withContext(Dispatchers.IO) {
                    repository.getAllModelsSync()
                }
                _allModels.value = models
            } catch (e: Exception) {
                _errorMessage.value = "Error loading models: ${e.message}"
            }
        }
    }
    
    /**
     * Set the category filter
     */
    fun setModelCategoryFilter(category: String?) {
        _currentCategoryFilter.value = category
    }
    
    /**
     * Select a model by ID
     */
    fun selectModel(modelId: Long) {
        viewModelScope.launch {
            try {
                val model = withContext(Dispatchers.IO) {
                    repository.getModelById(modelId)
                }
                _selectedModel.value = model
            } catch (e: Exception) {
                _errorMessage.value = "Error selecting model: ${e.message}"
            }
        }
    }
    
    /**
     * Get a model by ID
     */
    fun getModelById(modelId: Long): LiveData<ARModel?> {
        val result = MutableLiveData<ARModel?>()
        viewModelScope.launch {
            try {
                val model = withContext(Dispatchers.IO) {
                    repository.getModelById(modelId)
                }
                result.value = model
            } catch (e: Exception) {
                _errorMessage.value = "Error getting model: ${e.message}"
                result.value = null
            }
        }
        return result
    }
    
    /**
     * Download a model
     */
    fun downloadModel(modelId: Long) {
        viewModelScope.launch {
            try {
                // Check if model exists
                val model = withContext(Dispatchers.IO) {
                    repository.getModelById(modelId)
                } ?: throw Exception("Model not found")
                
                // Start download
                _downloadProgress.value = DownloadStatus.InProgress(modelId, 0)
                
                // Simulate download progress (in a real app, this would be actual download logic)
                for (progress in 10..100 step 10) {
                    _downloadProgress.value = DownloadStatus.InProgress(modelId, progress)
                    delay(500) // Simulate network delay
                }
                
                // Mark model as downloaded
                withContext(Dispatchers.IO) {
                    repository.markModelAsDownloaded(modelId)
                }
                
                // Update download status
                _downloadProgress.value = DownloadStatus.Success(modelId)
                
                // Refresh models to update UI
                refreshModels()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error downloading model: ${e.message}"
                _downloadProgress.value = DownloadStatus.Failed(modelId, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Check if a model is downloaded
     */
    suspend fun isModelDownloaded(modelId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            repository.isModelDownloaded(modelId)
        }
    }
    
    /**
     * Set whether a model is placed in AR
     */
    fun setModelPlaced(isPlaced: Boolean) {
        _isModelPlaced.value = isPlaced
    }
    
    /**
     * Update the AR tracking status
     */
    fun updateTrackingStatus(status: TrackingStatus) {
        _trackingStatus.value = status
    }
    
    /**
     * Clear the error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Format file size for display
     */
    fun getFormattedFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> "${sizeInBytes / (1024 * 1024)} MB"
        }
    }
    
    /**
     * AR tracking status
     */
    enum class TrackingStatus {
        TRACKING_NORMAL,
        TRACKING_LIMITED,
        NOT_TRACKING
    }
    
    /**
     * Download status
     */
    sealed class DownloadStatus {
        data class InProgress(val modelId: Long, val progress: Int) : DownloadStatus()
        data class Success(val modelId: Long) : DownloadStatus()
        data class Failed(val modelId: Long, val error: String) : DownloadStatus()
    }
    
    // Helper function to simulate delay
    private suspend fun delay(timeMillis: Long) {
        withContext(Dispatchers.IO) {
            Thread.sleep(timeMillis)
        }
    }
}
