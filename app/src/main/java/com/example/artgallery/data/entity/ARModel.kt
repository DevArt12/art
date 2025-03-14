package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "ar_models",
    foreignKeys = [
        ForeignKey(
            entity = Artwork::class,
            parentColumns = ["id"],
            childColumns = ["relatedArtworkId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("relatedArtworkId"), Index("artistId")]
)
data class ARModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val modelFilePath: String, // Path to the .glb or .sfb file
    val thumbnailPath: String,
    val artistId: Long,
    val relatedArtworkId: Long? = null, // Can be null if not related to a specific artwork
    val category: String,
    val scale: Float = 1.0f, // Default scale for the model
    val dateAdded: Long,
    val fileSize: Long, // Size in bytes
    val isDownloaded: Boolean = false,
    val isAnimated: Boolean = false,
    val animationDuration: Int? = null, // Duration in milliseconds if animated
    val interactionType: String = INTERACTION_TYPE_STATIC,
    val boundingBoxWidth: Float? = null,
    val boundingBoxHeight: Float? = null,
    val boundingBoxDepth: Float? = null
) : Parcelable {
    companion object {
        const val CATEGORY_SCULPTURE = "sculpture"
        const val CATEGORY_PAINTING = "painting"
        const val CATEGORY_INSTALLATION = "installation"
        const val CATEGORY_CHARACTER = "character"
        const val CATEGORY_FURNITURE = "furniture"
        const val CATEGORY_ABSTRACT = "abstract"
        
        const val INTERACTION_TYPE_STATIC = "static"
        const val INTERACTION_TYPE_ROTATABLE = "rotatable"
        const val INTERACTION_TYPE_ANIMATED = "animated"
        const val INTERACTION_TYPE_INTERACTIVE = "interactive"
    }
}
