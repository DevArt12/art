package com.example.artgallery.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    private const val JPEG_EXTENSION = ".jpg"

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            JPEG_EXTENSION,
            storageDir
        )
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = createImageFile(context)
        
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        
        inputStream?.close()
        return file.absolutePath
    }

    fun deleteImage(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
