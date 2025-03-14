package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "performances",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("artistId")]
)
data class Performance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val videoPath: String,
    val artistId: Long,
    val duration: Int, // in seconds
    val thumbnailPath: String,
    val category: String,
    val dateRecorded: Long,
    val viewCount: Int = 0,
    val isDownloaded: Boolean = false
) : Parcelable {
    companion object {
        const val CATEGORY_MUSIC = "music"
        const val CATEGORY_DANCE = "dance"
        const val CATEGORY_THEATER = "theater"
        const val CATEGORY_MIXED_MEDIA = "mixed_media"
    }
}
