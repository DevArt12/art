package com.example.artgallery.repository

import androidx.lifecycle.LiveData
import com.example.artgallery.data.dao.ArtNewsDao
import com.example.artgallery.data.entity.ArtNews

class ArtNewsRepository(private val artNewsDao: ArtNewsDao) {
    fun getAllNews(): LiveData<List<ArtNews>> = artNewsDao.getAllNews()

    suspend fun insertNews(news: ArtNews): Long = artNewsDao.insertNews(news)

    suspend fun updateNews(news: ArtNews) = artNewsDao.updateNews(news)

    suspend fun deleteNews(news: ArtNews) = artNewsDao.deleteNews(news)
}
