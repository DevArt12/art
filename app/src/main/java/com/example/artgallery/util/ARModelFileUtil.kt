package com.example.artgallery.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.artgallery.data.entity.ARModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Utility class for handling AR model file operations
 */
class ARModelFileUtil(private val context: Context) {

    companion object {
        private const val TAG = "ARModelFileUtil"
        private const val MODELS_DIR = "ar_models"
        private const val THUMBNAILS_DIR = "ar_thumbnails"
        private const val BUFFER_SIZE = 4096
    }

    /**
     * Get the directory for storing AR models
     */
    fun getModelsDirectory(): File {
        val modelsDir = File(context.filesDir, MODELS_DIR)
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return modelsDir
    }

    /**
     * Get the directory for storing AR model thumbnails
     */
    fun getThumbnailsDirectory(): File {
        val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIR)
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs()
        }
        return thumbnailsDir
    }

    /**
     * Get the file for a specific AR model
     */
    fun getModelFile(model: ARModel): File {
        return File(getModelsDirectory(), "${model.id}_${model.name.replace(" ", "_")}.glb")
    }

    /**
     * Get the file for a specific AR model thumbnail
     */
    fun getModelThumbnailFile(model: ARModel): File {
        return File(getThumbnailsDirectory(), "${model.id}_${model.name.replace(" ", "_")}.jpg")
    }

    /**
     * Check if a model file exists
     */
    fun modelFileExists(model: ARModel): Boolean {
        return getModelFile(model).exists()
    }

    /**
     * Check if a model thumbnail file exists
     */
    fun modelThumbnailExists(model: ARModel): Boolean {
        return getModelThumbnailFile(model).exists()
    }

    /**
     * Download a model file from a URL
     * @return true if download was successful, false otherwise
     */
    suspend fun downloadModelFile(model: ARModel, modelUrl: String): Boolean = suspendCoroutine { continuation ->
        try {
            val url = URL(modelUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                continuation.resume(false)
                return@suspendCoroutine
            }

            val fileLength = connection.contentLength
            val modelFile = getModelFile(model)
            
            // Download the file
            connection.inputStream.use { input ->
                FileOutputStream(modelFile).use { output ->
                    val data = ByteArray(BUFFER_SIZE)
                    var count: Int
                    var total = 0
                    
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        output.write(data, 0, count)
                        
                        // Report progress
                        if (fileLength > 0) {
                            val progress = (total * 100 / fileLength)
                            Log.d(TAG, "Download progress: $progress%")
                        }
                    }
                }
            }
            
            continuation.resume(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model file", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Download a model thumbnail from a URL
     * @return true if download was successful, false otherwise
     */
    suspend fun downloadModelThumbnail(model: ARModel, thumbnailUrl: String): Boolean = suspendCoroutine { continuation ->
        try {
            val url = URL(thumbnailUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                continuation.resume(false)
                return@suspendCoroutine
            }

            val thumbnailFile = getModelThumbnailFile(model)
            
            // Download the thumbnail
            connection.inputStream.use { input ->
                FileOutputStream(thumbnailFile).use { output ->
                    val data = ByteArray(BUFFER_SIZE)
                    var count: Int
                    
                    while (input.read(data).also { count = it } != -1) {
                        output.write(data, 0, count)
                    }
                }
            }
            
            continuation.resume(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model thumbnail", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Copy a model file from assets to the app's file directory
     */
    fun copyModelFromAssets(assetFileName: String, model: ARModel): Boolean {
        return try {
            val modelFile = getModelFile(model)
            context.assets.open(assetFileName).use { input ->
                FileOutputStream(modelFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model from assets", e)
            false
        }
    }

    /**
     * Copy a model thumbnail from assets to the app's file directory
     */
    fun copyThumbnailFromAssets(assetFileName: String, model: ARModel): Boolean {
        return try {
            val thumbnailFile = getModelThumbnailFile(model)
            context.assets.open(assetFileName).use { input ->
                FileOutputStream(thumbnailFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy thumbnail from assets", e)
            false
        }
    }

    /**
     * Delete a model file
     */
    fun deleteModelFile(model: ARModel): Boolean {
        val modelFile = getModelFile(model)
        return if (modelFile.exists()) {
            modelFile.delete()
        } else {
            true // File doesn't exist, so consider it deleted
        }
    }

    /**
     * Delete a model thumbnail file
     */
    fun deleteModelThumbnail(model: ARModel): Boolean {
        val thumbnailFile = getModelThumbnailFile(model)
        return if (thumbnailFile.exists()) {
            thumbnailFile.delete()
        } else {
            true // File doesn't exist, so consider it deleted
        }
    }

    /**
     * Get the Uri for a model file
     */
    fun getModelFileUri(model: ARModel): Uri {
        return Uri.fromFile(getModelFile(model))
    }

    /**
     * Get the Uri for a model thumbnail file
     */
    fun getModelThumbnailUri(model: ARModel): Uri {
        return Uri.fromFile(getModelThumbnailFile(model))
    }

    /**
     * Get the size of a model file in bytes
     */
    fun getModelFileSize(model: ARModel): Long {
        val modelFile = getModelFile(model)
        return if (modelFile.exists()) {
            modelFile.length()
        } else {
            0
        }
    }
}
