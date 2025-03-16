package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.Performance

@Dao
interface PerformanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: Performance): Long

    @Update
    suspend fun updatePerformance(performance: Performance)

    @Delete
    suspend fun deletePerformance(performance: Performance)

    @Query("SELECT * FROM performances ORDER BY dateCreated DESC")
    fun getAllPerformances(): LiveData<List<Performance>>

    @Query("SELECT * FROM performances ORDER BY dateCreated DESC")
    suspend fun getAllPerformancesSync(): List<Performance>

    @Query("SELECT * FROM performances WHERE id = :performanceId")
    suspend fun getPerformanceById(performanceId: Long): Performance?

    @Query("SELECT * FROM performances WHERE artistId = :artistId ORDER BY dateCreated DESC")
    fun getPerformancesByArtist(artistId: Long): LiveData<List<Performance>>

    @Query("SELECT * FROM performances WHERE category = :category ORDER BY dateCreated DESC")
    fun getPerformancesByCategory(category: String): LiveData<List<Performance>>

    @Query("SELECT * FROM performances WHERE isDownloaded = 1 ORDER BY dateCreated DESC")
    fun getDownloadedPerformances(): LiveData<List<Performance>>

    @Query("UPDATE performances SET viewCount = viewCount + 1 WHERE id = :performanceId")
    suspend fun incrementViewCount(performanceId: Long)
}
