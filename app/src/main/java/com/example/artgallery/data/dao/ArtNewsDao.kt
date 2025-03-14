package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.ArtNews

@Dao
interface ArtNewsDao {
    @Query("SELECT * FROM art_news ORDER BY date DESC")
    fun getAllNews(): LiveData<List<ArtNews>>

    @Insert
    suspend fun insertNews(news: ArtNews): Long

    @Update
    suspend fun updateNews(news: ArtNews)

    @Delete
    suspend fun deleteNews(news: ArtNews)
}
