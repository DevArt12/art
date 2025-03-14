package com.example.artgallery.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for file operations
 */
object FileUtil {
    
    /**
     * Creates a temporary image file in the app's private storage
     */
    fun createImageFile(context: Context): File {
        // Create an image file name with timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        
        // Get the app's private pictures directory
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
        // Create the file
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
    
    /**
     * Copies an image from a Uri to a destination file
     */
    fun copyImageToFile(context: Context, sourceUri: Uri, destinationFile: File) {
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Gets a file size in a human-readable format
     */
    fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
    
    /**
     * Gets the file extension from a file path
     */
    fun getFileExtension(filePath: String): String {
        return filePath.substring(filePath.lastIndexOf(".") + 1)
    }
    
    /**
     * Checks if a file is an image based on its extension
     */
    fun isImageFile(filePath: String): Boolean {
        val extension = getFileExtension(filePath).lowercase(Locale.getDefault())
        return extension == "jpg" || extension == "jpeg" || extension == "png" || extension == "gif" || extension == "webp"
    }
}
