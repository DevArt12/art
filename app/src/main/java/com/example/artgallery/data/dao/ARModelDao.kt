package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.ARModel

@Dao
interface ARModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertARModel(arModel: ARModel): Long

    @Update
    suspend fun updateARModel(arModel: ARModel)

    @Delete
    suspend fun deleteARModel(arModel: ARModel)

    @Query("SELECT * FROM ar_models")
    fun getAllModels(): LiveData<List<ARModel>>

    @Query("SELECT * FROM ar_models")
    suspend fun getAllModelsSync(): List<ARModel>

    @Query("SELECT * FROM ar_models WHERE id = :modelId")
    suspend fun getModelById(modelId: Long): ARModel?

    @Query("SELECT * FROM ar_models WHERE artistId = :artistId")
    fun getModelsByArtist(artistId: Long): LiveData<List<ARModel>>

    @Query("SELECT * FROM ar_models WHERE relatedArtworkId = :artworkId")
    fun getModelsForArtwork(artworkId: Long): LiveData<List<ARModel>>

    @Query("SELECT * FROM ar_models WHERE category = :category")
    fun getModelsByCategory(category: String): LiveData<List<ARModel>>

    @Query("SELECT * FROM ar_models WHERE isDownloaded = 1")
    fun getDownloadedModels(): LiveData<List<ARModel>>

    @Query("UPDATE ar_models SET isDownloaded = :isDownloaded WHERE id = :modelId")
    suspend fun updateDownloadStatus(modelId: Long, isDownloaded: Boolean)

    @Query("SELECT * FROM ar_models WHERE interactionType = :interactionType")
    fun getModelsByInteractionType(interactionType: String): LiveData<List<ARModel>>

    @Query("SELECT SUM(fileSize) FROM ar_models WHERE isDownloaded = 1")
    suspend fun getTotalDownloadedSize(): Long?
}
