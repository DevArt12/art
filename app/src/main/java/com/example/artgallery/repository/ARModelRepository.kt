package com.example.artgallery.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.ARModel
import com.example.artgallery.util.ARModelFileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository for managing AR models, including database operations and file management
 */
class ARModelRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val arModelDao = database.arModelDao()
    private val fileUtil = ARModelFileUtil(context)
    
    // Get all AR models from the database
    fun getAllModels(): LiveData<List<ARModel>> {
        return arModelDao.getAllModels()
    }
    
    // Get all AR models synchronously (non-LiveData)
    suspend fun getAllModelsSync(): List<ARModel> {
        return arModelDao.getAllModelsSync()
    }
    
    // Get AR models by category
    fun getModelsByCategory(category: String): LiveData<List<ARModel>> {
        return arModelDao.getModelsByCategory(category)
    }
    
    // Get AR models related to a specific artwork
    fun getModelsForArtwork(artworkId: Long): LiveData<List<ARModel>> {
        return arModelDao.getModelsForArtwork(artworkId)
    }
    
    // Get downloaded AR models
    fun getDownloadedModels(): LiveData<List<ARModel>> {
        return arModelDao.getDownloadedModels()
    }
    
    // Get AR model by ID
    suspend fun getModelById(modelId: Long): ARModel? {
        return arModelDao.getModelById(modelId)
    }
    
    // Insert a new AR model
    suspend fun insertARModel(arModel: ARModel): Long {
        return arModelDao.insertARModel(arModel)
    }
    
    // Update an existing AR model
    suspend fun updateARModel(arModel: ARModel) {
        arModelDao.updateARModel(arModel)
    }
    
    // Delete an AR model
    suspend fun deleteARModel(arModel: ARModel) {
        // Delete the model file and thumbnail if they exist
        fileUtil.deleteModelFile(arModel)
        fileUtil.deleteModelThumbnail(arModel)
        
        // Delete from database
        arModelDao.deleteARModel(arModel)
    }
    
    // Mark a model as downloaded
    suspend fun markModelAsDownloaded(modelId: Long) {
        val model = arModelDao.getModelById(modelId) ?: return
        
        // In a real app, this would check if the file exists and is valid
        // For this example, we'll just mark it as downloaded
        val updatedModel = model.copy(isDownloaded = true)
        arModelDao.updateARModel(updatedModel)
    }
    
    // Download a model from a URL
    suspend fun downloadModel(model: ARModel, modelUrl: String, thumbnailUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Download model file
                val modelDownloaded = fileUtil.downloadModelFile(model, modelUrl)
                
                // Download thumbnail
                val thumbnailDownloaded = fileUtil.downloadModelThumbnail(model, thumbnailUrl)
                
                if (modelDownloaded && thumbnailDownloaded) {
                    // Update the model with file paths and download status
                    val updatedModel = model.copy(
                        modelFilePath = fileUtil.getModelFile(model).absolutePath,
                        thumbnailPath = fileUtil.getModelThumbnailFile(model).absolutePath,
                        fileSize = fileUtil.getModelFileSize(model),
                        isDownloaded = true
                    )
                    
                    // Update the database
                    arModelDao.updateARModel(updatedModel)
                    true
                } else {
                    // Clean up any partially downloaded files
                    if (modelDownloaded) fileUtil.deleteModelFile(model)
                    if (thumbnailDownloaded) fileUtil.deleteModelThumbnail(model)
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    // Load preloaded models from assets (for offline first approach)
    suspend fun loadPreloadedModels() {
        withContext(Dispatchers.IO) {
            try {
                // Get list of preloaded models from assets directory
                val assetManager = context.assets
                val preloadedModelsDir = "preloaded_ar_models"
                val preloadedModels = assetManager.list(preloadedModelsDir) ?: return@withContext
                
                for (modelFileName in preloadedModels) {
                    if (modelFileName.endsWith(".glb")) {
                        val modelId = modelFileName.substringBefore(".").toLongOrNull() ?: continue
                        val modelName = modelFileName.substringBefore(".").replace("_", " ")
                        
                        // Check if model already exists in database
                        val existingModel = arModelDao.getModelById(modelId)
                        if (existingModel == null) {
                            // Create placeholder entry in database
                            val arModel = ARModel(
                                id = modelId,
                                name = modelName,
                                description = "This is a preloaded AR model",
                                modelFilePath = "", // Will be set after copying
                                thumbnailPath = "", // Will be set after copying
                                artistId = 1, // Default artist ID
                                category = ARModel.CATEGORY_SCULPTURE,
                                dateAdded = System.currentTimeMillis(),
                                fileSize = 0, // Will be updated after copying
                                isDownloaded = false
                            )
                            
                            // Insert the model to get an ID
                            val newModelId = arModelDao.insertARModel(arModel)
                            
                            // Retrieve the newly inserted model
                            val newModel = arModelDao.getModelById(newModelId) ?: continue
                            
                            // Copy model file from assets
                            val modelAssetPath = "$preloadedModelsDir/$modelFileName"
                            val thumbnailAssetPath = "$preloadedModelsDir/${modelFileName.replace(".glb", ".jpg")}"
                            
                            val modelCopied = fileUtil.copyModelFromAssets(modelAssetPath, newModel)
                            val thumbnailCopied = fileUtil.copyThumbnailFromAssets(thumbnailAssetPath, newModel)
                            
                            if (modelCopied && thumbnailCopied) {
                                // Update the model with file paths and download status
                                val updatedModel = newModel.copy(
                                    modelFilePath = fileUtil.getModelFile(newModel).absolutePath,
                                    thumbnailPath = fileUtil.getModelThumbnailFile(newModel).absolutePath,
                                    fileSize = fileUtil.getModelFileSize(newModel),
                                    isDownloaded = true
                                )
                                
                                // Update the database
                                arModelDao.updateARModel(updatedModel)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    // Check if a model is downloaded
    suspend fun isModelDownloaded(modelId: Long): Boolean {
        val model = arModelDao.getModelById(modelId) ?: return false
        return model.isDownloaded && fileUtil.modelFileExists(model)
    }
    
    // Get total size of downloaded AR models
    suspend fun getTotalDownloadedSize(): Long {
        return arModelDao.getTotalDownloadedSize() ?: 0L
    }
    
    // Get model file path
    fun getModelFilePath(model: ARModel): String {
        return fileUtil.getModelFile(model).absolutePath
    }
    
    // Get model thumbnail path
    fun getModelThumbnailPath(model: ARModel): String {
        return fileUtil.getModelThumbnailFile(model).absolutePath
    }
}
