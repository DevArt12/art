package com.example.artgallery.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utility class for formatting file sizes in a human-readable format
 */
object FileSizeFormatter {
    private val units = arrayOf("B", "KB", "MB", "GB", "TB")
    
    /**
     * Format a file size in bytes to a human-readable string
     * @param size File size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val formatter = DecimalFormat("#,##0.#")
        
        return formatter.format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
