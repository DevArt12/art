package com.example.artgallery.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.Performance
import com.example.artgallery.util.PerformanceFileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository for managing performances, including database operations and file management
 */
class PerformanceRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val performanceDao = database.performanceDao()
    private val fileUtil = PerformanceFileUtil(context)
    
    // Get all performances from the database
    fun getAllPerformances(): LiveData<List<Performance>> {
        return performanceDao.getAllPerformances()
    }
    
    // Get all performances synchronously (non-LiveData)
    suspend fun getAllPerformancesSync(): List<Performance> {
        return performanceDao.getAllPerformancesSync()
    }
    
    // Get performances by category
    fun getPerformancesByCategory(category: String): LiveData<List<Performance>> {
        return performanceDao.getPerformancesByCategory(category)
    }
    
    // Get performances by artist
    fun getPerformancesByArtist(artistId: Long): LiveData<List<Performance>> {
        return performanceDao.getPerformancesByArtist(artistId)
    }
    
    // Get downloaded performances
    fun getDownloadedPerformances(): LiveData<List<Performance>> {
        return performanceDao.getDownloadedPerformances()
    }
    
    // Get performance by ID
    suspend fun getPerformanceById(performanceId: Long): Performance? {
        return performanceDao.getPerformanceById(performanceId)
    }
    
    // Insert a new performance
    suspend fun insertPerformance(performance: Performance): Long {
        return performanceDao.insertPerformance(performance)
    }
    
    // Update an existing performance
    suspend fun updatePerformance(performance: Performance) {
        performanceDao.updatePerformance(performance)
    }
    
    // Delete a performance
    suspend fun deletePerformance(performance: Performance) {
        // Delete the video file and thumbnail if they exist
        fileUtil.deleteVideoFile(performance)
        fileUtil.deleteThumbnail(performance)
        
        // Delete from database
        performanceDao.deletePerformance(performance)
    }
    
    // Mark a performance as downloaded
    suspend fun markPerformanceAsDownloaded(performanceId: Long) {
        val performance = performanceDao.getPerformanceById(performanceId) ?: return
        
        // In a real app, this would check if the file exists and is valid
        // For this example, we'll just mark it as downloaded
        val updatedPerformance = performance.copy(isDownloaded = true)
        performanceDao.updatePerformance(updatedPerformance)
    }
    
    // Download a performance video
    suspend fun downloadPerformance(performance: Performance, videoUrl: String, thumbnailUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Download video file
                val videoDownloaded = fileUtil.downloadVideoFile(performance, videoUrl)
                
                // Download thumbnail
                val thumbnailDownloaded = fileUtil.downloadThumbnail(performance, thumbnailUrl)
                
                if (videoDownloaded && thumbnailDownloaded) {
                    // Update the performance with file paths and download status
                    val updatedPerformance = performance.copy(
                        videoPath = fileUtil.getVideoFile(performance).absolutePath,
                        thumbnailPath = fileUtil.getThumbnailFile(performance).absolutePath,
                        isDownloaded = true
                    )
                    
                    // Update the database
                    performanceDao.updatePerformance(updatedPerformance)
                    true
                } else {
                    // Clean up any partially downloaded files
                    if (videoDownloaded) fileUtil.deleteVideoFile(performance)
                    if (thumbnailDownloaded) fileUtil.deleteThumbnail(performance)
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    // Increment view count for a performance
    suspend fun incrementViewCount(performanceId: Long) {
        val performance = performanceDao.getPerformanceById(performanceId) ?: return
        val updatedPerformance = performance.copy(viewCount = performance.viewCount + 1)
        performanceDao.updatePerformance(updatedPerformance)
    }
    
    // Load preloaded performances from assets (for offline first approach)
    suspend fun loadPreloadedPerformances() {
        withContext(Dispatchers.IO) {
            try {
                // Get list of preloaded performances from assets directory
                val assetManager = context.assets
                val preloadedPerformancesDir = "preloaded_performances"
                val preloadedPerformances = assetManager.list(preloadedPerformancesDir) ?: return@withContext
                
                for (videoFileName in preloadedPerformances) {
                    if (videoFileName.endsWith(".mp4")) {
                        val performanceId = videoFileName.substringBefore(".").toLongOrNull() ?: continue
                        val performanceName = videoFileName.substringBefore(".").replace("_", " ")
                        
                        // Check if performance already exists in database
                        val existingPerformance = performanceDao.getPerformanceById(performanceId)
                        if (existingPerformance == null) {
                            // Create placeholder entry in database
                            val performance = Performance(
                                id = performanceId,
                                title = performanceName,
                                description = "This is a preloaded performance",
                                videoPath = "", // Will be set after copying
                                artistId = 1, // Default artist ID
                                duration = 180, // Default duration (3 minutes)
                                thumbnailPath = "", // Will be set after copying
                                category = Performance.CATEGORY_MUSIC,
                                dateRecorded = System.currentTimeMillis(),
                                viewCount = 0,
                                isDownloaded = false
                            )
                            
                            // Insert the performance to get an ID
                            val newPerformanceId = performanceDao.insertPerformance(performance)
                            
                            // Retrieve the newly inserted performance
                            val newPerformance = performanceDao.getPerformanceById(newPerformanceId) ?: continue
                            
                            // Copy video file from assets
                            val videoAssetPath = "$preloadedPerformancesDir/$videoFileName"
                            val thumbnailAssetPath = "$preloadedPerformancesDir/${videoFileName.replace(".mp4", ".jpg")}"
                            
                            val videoCopied = fileUtil.copyVideoFromAssets(videoAssetPath, newPerformance)
                            val thumbnailCopied = fileUtil.copyThumbnailFromAssets(thumbnailAssetPath, newPerformance)
                            
                            if (videoCopied && thumbnailCopied) {
                                // Update the performance with file paths and download status
                                val updatedPerformance = newPerformance.copy(
                                    videoPath = fileUtil.getVideoFile(newPerformance).absolutePath,
                                    thumbnailPath = fileUtil.getThumbnailFile(newPerformance).absolutePath,
                                    isDownloaded = true
                                )
                                
                                // Update the database
                                performanceDao.updatePerformance(updatedPerformance)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    // Check if a performance is downloaded
    suspend fun isPerformanceDownloaded(performanceId: Long): Boolean {
        val performance = performanceDao.getPerformanceById(performanceId) ?: return false
        return performance.isDownloaded && fileUtil.videoFileExists(performance)
    }
    
    // Get video file path
    fun getVideoFilePath(performance: Performance): String {
        return fileUtil.getVideoFile(performance).absolutePath
    }
    
    // Get thumbnail path
    fun getThumbnailPath(performance: Performance): String {
        return fileUtil.getThumbnailFile(performance).absolutePath
    }
}
