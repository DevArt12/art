package com.example.artgallery.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.util.TutorialFileUtil
import java.io.File

/**
 * Repository for managing tutorials, including database operations and file management
 */
class TutorialRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val tutorialDao = database.tutorialDao()
    private val fileUtil = TutorialFileUtil(context)
    
    // Get all tutorials as LiveData
    fun getAllTutorials(): LiveData<List<Tutorial>> {
        return tutorialDao.getAllTutorials()
    }
    
    // Get all tutorials synchronously
    suspend fun getAllTutorialsSync(): List<Tutorial> {
        return tutorialDao.getAllTutorialsSync()
    }
    
    // Get tutorials by category
    fun getTutorialsByCategory(category: String): LiveData<List<Tutorial>> {
        return tutorialDao.getTutorialsByCategory(category)
    }
    
    // Get tutorials by difficulty
    fun getTutorialsByDifficulty(difficulty: String): LiveData<List<Tutorial>> {
        return tutorialDao.getTutorialsByDifficulty(difficulty)
    }
    
    // Get tutorials by category and difficulty
    fun getTutorialsByCategoryAndDifficulty(category: String, difficulty: String): LiveData<List<Tutorial>> {
        return tutorialDao.getTutorialsByCategoryAndDifficulty(category, difficulty)
    }
    
    // Get tutorial by ID
    suspend fun getTutorialById(tutorialId: Long): Tutorial? {
        return tutorialDao.getTutorialById(tutorialId)
    }
    
    // Insert a new tutorial
    suspend fun insertTutorial(tutorial: Tutorial): Long {
        return tutorialDao.insertTutorial(tutorial)
    }
    
    // Update an existing tutorial
    suspend fun updateTutorial(tutorial: Tutorial) {
        tutorialDao.updateTutorial(tutorial)
    }
    
    // Delete a tutorial
    suspend fun deleteTutorial(tutorial: Tutorial) {
        // Delete associated files
        deleteTutorialFiles(tutorial)
        
        // Delete from database
        tutorialDao.deleteTutorial(tutorial)
    }
    
    // Delete tutorial files
    suspend fun deleteTutorialFiles(tutorial: Tutorial) {
        // Delete video file
        fileUtil.deleteVideoFile(tutorial)
        
        // Delete thumbnail
        fileUtil.deleteThumbnail(tutorial)
    }
    
    // Increment view count for a tutorial
    suspend fun incrementViewCount(tutorialId: Long) {
        val tutorial = tutorialDao.getTutorialById(tutorialId) ?: return
        val updatedTutorial = tutorial.copy(viewCount = tutorial.viewCount + 1)
        tutorialDao.updateTutorial(updatedTutorial)
    }
    
    // Update user progress for a tutorial
    suspend fun updateUserProgress(tutorialId: Long, progress: Int) {
        val tutorial = tutorialDao.getTutorialById(tutorialId) ?: return
        val updatedTutorial = tutorial.copy(userProgress = progress)
        tutorialDao.updateTutorial(updatedTutorial)
    }
    
    // Mark tutorial quiz as completed
    suspend fun markQuizCompleted(tutorialId: Long) {
        val tutorial = tutorialDao.getTutorialById(tutorialId) ?: return
        val updatedTutorial = tutorial.copy(hasCompletedQuiz = true)
        tutorialDao.updateTutorial(updatedTutorial)
    }
    
    // Download a tutorial video
    suspend fun downloadTutorialVideo(tutorial: Tutorial, videoUrl: String): Boolean {
        return fileUtil.downloadVideoFile(tutorial, videoUrl)
    }
    
    // Download a tutorial thumbnail
    suspend fun downloadTutorialThumbnail(tutorial: Tutorial, thumbnailUrl: String): Boolean {
        return fileUtil.downloadThumbnail(tutorial, thumbnailUrl)
    }
    
    // Search tutorials by query
    suspend fun searchTutorials(query: String): List<Tutorial> {
        return tutorialDao.searchTutorials("%$query%")
    }
    
    // Get downloaded tutorials
    fun getDownloadedTutorials(): LiveData<List<Tutorial>> {
        return tutorialDao.getDownloadedTutorials()
    }
    
    // Get completed tutorials (with completed quiz)
    fun getCompletedTutorials(): LiveData<List<Tutorial>> {
        return tutorialDao.getCompletedTutorials()
    }
    
    // Get in-progress tutorials (with progress > 0 but not completed)
    fun getInProgressTutorials(): LiveData<List<Tutorial>> {
        return tutorialDao.getInProgressTutorials()
    }
    
    // Get video file for a tutorial
    fun getVideoFile(tutorial: Tutorial): File {
        return fileUtil.getVideoFile(tutorial)
    }
    
    // Check if video file exists
    fun videoFileExists(tutorial: Tutorial): Boolean {
        return fileUtil.videoFileExists(tutorial)
    }
    
    // Copy tutorial video from assets
    suspend fun copyVideoFromAssets(assetPath: String, tutorial: Tutorial): Boolean {
        return fileUtil.copyVideoFromAssets(assetPath, tutorial)
    }
    
    // Copy tutorial thumbnail from assets
    suspend fun copyThumbnailFromAssets(assetPath: String, tutorial: Tutorial): Boolean {
        return fileUtil.copyThumbnailFromAssets(assetPath, tutorial)
    }
}
