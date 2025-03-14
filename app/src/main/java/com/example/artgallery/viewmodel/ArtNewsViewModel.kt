package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.ArtNews
import com.example.artgallery.repository.ArtNewsRepository
import kotlinx.coroutines.launch

class ArtNewsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ArtNewsRepository
    val allNews: LiveData<List<ArtNews>>

    init {
        val artNewsDao = AppDatabase.getDatabase(application).artNewsDao()
        repository = ArtNewsRepository(artNewsDao)
        allNews = repository.getAllNews()
    }

    fun insertNews(news: ArtNews) = viewModelScope.launch {
        repository.insertNews(news)
    }

    fun updateNews(news: ArtNews) = viewModelScope.launch {
        repository.updateNews(news)
    }

    fun deleteNews(news: ArtNews) = viewModelScope.launch {
        repository.deleteNews(news)
    }
}
