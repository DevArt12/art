package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "tutorials",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["instructorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("instructorId")]
)
data class Tutorial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val videoPath: String,
    val instructorId: Long,
    val duration: Int, // in seconds
    val thumbnailPath: String,
    val category: String,
    val difficulty: String,
    val materialsNeeded: String? = null,
    val dateAdded: Long,
    val viewCount: Int = 0,
    val isDownloaded: Boolean = false,
    val hasCompletedQuiz: Boolean = false,
    val userProgress: Int = 0 // Percentage of completion
) : Parcelable {
    companion object {
        const val CATEGORY_PAINTING = "painting"
        const val CATEGORY_DRAWING = "drawing"
        const val CATEGORY_SCULPTURE = "sculpture"
        const val CATEGORY_DIGITAL = "digital"
        const val CATEGORY_MIXED_MEDIA = "mixed_media"
        
        const val DIFFICULTY_BEGINNER = "beginner"
        const val DIFFICULTY_INTERMEDIATE = "intermediate"
        const val DIFFICULTY_ADVANCED = "advanced"
    }
}
package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tutorials")
data class Tutorial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val videoPath: String,
    val category: String,
    val duration: Int,
    val instructorName: String,
    val thumbnailPath: String,
    val isDownloaded: Boolean = false
) : Parcelable
