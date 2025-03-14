package com.example.artgallery.util

import android.content.Context
import com.example.artgallery.data.entity.Performance
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for managing performance video files and thumbnails
 */
class PerformanceFileUtil(private val context: Context) {

    // Get the directory for storing performance videos
    private fun getPerformanceDirectory(): File {
        val dir = File(context.filesDir, "performances")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Get the directory for storing performance thumbnails
    private fun getThumbnailDirectory(): File {
        val dir = File(context.filesDir, "performance_thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    // Get the video file for a performance
    fun getVideoFile(performance: Performance): File {
        return File(getPerformanceDirectory(), "performance_${performance.id}.mp4")
    }

    // Get the thumbnail file for a performance
    fun getThumbnailFile(performance: Performance): File {
        return File(getThumbnailDirectory(), "performance_thumbnail_${performance.id}.jpg")
    }

    // Check if video file exists
    fun videoFileExists(performance: Performance): Boolean {
        return getVideoFile(performance).exists()
    }

    // Check if thumbnail file exists
    fun thumbnailExists(performance: Performance): Boolean {
        return getThumbnailFile(performance).exists()
    }

    // Download a video file from a URL
    fun downloadVideoFile(performance: Performance, videoUrl: String): Boolean {
        return downloadFile(videoUrl, getVideoFile(performance))
    }

    // Download a thumbnail from a URL
    fun downloadThumbnail(performance: Performance, thumbnailUrl: String): Boolean {
        return downloadFile(thumbnailUrl, getThumbnailFile(performance))
    }

    // Delete a video file
    fun deleteVideoFile(performance: Performance): Boolean {
        val file = getVideoFile(performance)
        return if (file.exists()) file.delete() else true
    }

    // Delete a thumbnail
    fun deleteThumbnail(performance: Performance): Boolean {
        val file = getThumbnailFile(performance)
        return if (file.exists()) file.delete() else true
    }

    // Copy a video file from assets
    fun copyVideoFromAssets(assetPath: String, performance: Performance): Boolean {
        return try {
            val inputStream = context.assets.open(assetPath)
            val outputFile = getVideoFile(performance)
            copyFile(inputStream, outputFile)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Copy a thumbnail from assets
    fun copyThumbnailFromAssets(assetPath: String, performance: Performance): Boolean {
        return try {
            val inputStream = context.assets.open(assetPath)
            val outputFile = getThumbnailFile(performance)
            copyFile(inputStream, outputFile)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Helper method to download a file from a URL
    private fun downloadFile(fileUrl: String, destinationFile: File): Boolean {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null

        try {
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

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
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
