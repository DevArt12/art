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

    @Query("SELECT * FROM performances")
    fun getAllPerformances(): LiveData<List<Performance>>
    
    @Query("SELECT * FROM performances")
    suspend fun getAllPerformancesSync(): List<Performance>

    @Query("SELECT * FROM performances WHERE id = :performanceId")
    suspend fun getPerformanceById(performanceId: Long): Performance?

    @Query("SELECT * FROM performances WHERE artistId = :artistId")
    fun getPerformancesByArtist(artistId: Long): LiveData<List<Performance>>

    @Query("SELECT * FROM performances WHERE category = :category")
    fun getPerformancesByCategory(category: String): LiveData<List<Performance>>

    @Query("SELECT * FROM performances WHERE isDownloaded = 1")
    fun getDownloadedPerformances(): LiveData<List<Performance>>

    @Query("UPDATE performances SET viewCount = viewCount + 1 WHERE id = :performanceId")
    suspend fun incrementViewCount(performanceId: Long)
}
package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.Performance

@Dao
interface PerformanceDao {
    @Query("SELECT * FROM performances")
    fun getAllPerformances(): LiveData<List<Performance>>

    @Query("SELECT * FROM performances WHERE artistId = :artistId")
    fun getPerformancesByArtist(artistId: Long): LiveData<List<Performance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: Performance): Long

    @Update
    suspend fun updatePerformance(performance: Performance)

    @Delete
    suspend fun deletePerformance(performance: Performance)

    @Query("SELECT * FROM performances WHERE category = :category")
    fun getPerformancesByCategory(category: String): LiveData<List<Performance>>
}
