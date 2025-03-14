package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.Tutorial

@Dao
interface TutorialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorial(tutorial: Tutorial): Long

    @Update
    suspend fun updateTutorial(tutorial: Tutorial)

    @Delete
    suspend fun deleteTutorial(tutorial: Tutorial)

    @Query("SELECT * FROM tutorials")
    fun getAllTutorials(): LiveData<List<Tutorial>>
    
    @Query("SELECT * FROM tutorials")
    suspend fun getAllTutorialsSync(): List<Tutorial>

    @Query("SELECT * FROM tutorials WHERE id = :tutorialId")
    suspend fun getTutorialById(tutorialId: Long): Tutorial?

    @Query("SELECT * FROM tutorials WHERE instructorId = :instructorId")
    fun getTutorialsByInstructor(instructorId: Long): LiveData<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE category = :category")
    fun getTutorialsByCategory(category: String): LiveData<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE difficulty = :difficulty")
    fun getTutorialsByDifficulty(difficulty: String): LiveData<List<Tutorial>>
    
    @Query("SELECT * FROM tutorials WHERE category = :category AND difficulty = :difficulty")
    fun getTutorialsByCategoryAndDifficulty(category: String, difficulty: String): LiveData<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE isDownloaded = 1")
    fun getDownloadedTutorials(): LiveData<List<Tutorial>>
    
    @Query("SELECT * FROM tutorials WHERE hasCompletedQuiz = 1")
    fun getCompletedTutorials(): LiveData<List<Tutorial>>
    
    @Query("SELECT * FROM tutorials WHERE userProgress > 0 AND userProgress < 100")
    fun getInProgressTutorials(): LiveData<List<Tutorial>>
    
    @Query("SELECT * FROM tutorials WHERE title LIKE :query OR description LIKE :query")
    suspend fun searchTutorials(query: String): List<Tutorial>

    @Query("UPDATE tutorials SET viewCount = viewCount + 1 WHERE id = :tutorialId")
    suspend fun incrementViewCount(tutorialId: Long)

    @Query("UPDATE tutorials SET userProgress = :progress WHERE id = :tutorialId")
    suspend fun updateUserProgress(tutorialId: Long, progress: Int)

    @Query("UPDATE tutorials SET hasCompletedQuiz = 1 WHERE id = :tutorialId")
    suspend fun markQuizCompleted(tutorialId: Long)
}
