package com.example.artgallery.util

import android.content.Context
import com.example.artgallery.data.entity.Tutorial
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for managing tutorial video files and thumbnails
 */
class TutorialFileUtil(private val context: Context) {

    // Get the directory for storing tutorial videos
    private fun getTutorialDirectory(): File {
        val dir = File(context.filesDir, "tutorials")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Get the directory for storing tutorial thumbnails
    private fun getThumbnailDirectory(): File {
        val dir = File(context.filesDir, "tutorial_thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Get the video file for a tutorial
    fun getVideoFile(tutorial: Tutorial): File {
        return File(getTutorialDirectory(), "tutorial_${tutorial.id}.mp4")
    }

    // Get the thumbnail file for a tutorial
    fun getThumbnailFile(tutorial: Tutorial): File {
        return File(getThumbnailDirectory(), "tutorial_thumbnail_${tutorial.id}.jpg")
    }

    // Check if video file exists
    fun videoFileExists(tutorial: Tutorial): Boolean {
        return getVideoFile(tutorial).exists()
    }

    // Check if thumbnail file exists
    fun thumbnailExists(tutorial: Tutorial): Boolean {
        return getThumbnailFile(tutorial).exists()
    }

    // Download a video file from a URL
    suspend fun downloadVideoFile(tutorial: Tutorial, videoUrl: String): Boolean {
        return downloadFile(videoUrl, getVideoFile(tutorial))
    }

    // Download a thumbnail from a URL
    suspend fun downloadThumbnail(tutorial: Tutorial, thumbnailUrl: String): Boolean {
        return downloadFile(thumbnailUrl, getThumbnailFile(tutorial))
    }

    // Delete a video file
    fun deleteVideoFile(tutorial: Tutorial): Boolean {
        val file = getVideoFile(tutorial)
        return if (file.exists()) file.delete() else true
    }

    // Delete a thumbnail
    fun deleteThumbnail(tutorial: Tutorial): Boolean {
        val file = getThumbnailFile(tutorial)
        return if (file.exists()) file.delete() else true
    }

    // Copy a video file from assets
    fun copyVideoFromAssets(assetPath: String, tutorial: Tutorial): Boolean {
        return try {
            val inputStream = context.assets.open(assetPath)
            val outputFile = getVideoFile(tutorial)
            copyFile(inputStream, outputFile)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Copy a thumbnail from assets
    fun copyThumbnailFromAssets(assetPath: String, tutorial: Tutorial): Boolean {
        return try {
            val inputStream = context.assets.open(assetPath)
            val outputFile = getThumbnailFile(tutorial)
            copyFile(inputStream, outputFile)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Helper method to download a file from a URL
    private suspend fun downloadFile(fileUrl: String, destinationFile: File): Boolean {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null

        return try {
            val url = URL(fileUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // Check if the response is successful
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return false
            }

            // Download the file
            input = connection.inputStream
            output = FileOutputStream(destinationFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                output?.close()
                input?.close()
                connection?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Helper method to copy a file from an input stream to a file
    private fun copyFile(inputStream: InputStream, outputFile: File) {
        val outputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    }
}
