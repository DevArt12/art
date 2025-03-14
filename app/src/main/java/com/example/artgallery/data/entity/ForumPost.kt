package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "forum_posts",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("authorId")]
)
data class ForumPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val authorId: Long? = null, // Can be null if user is deleted but post remains
    val authorName: String, // Store name separately in case user is deleted
    val category: String,
    val createdAt: Long,
    val lastEditedAt: Long? = null,
    val imagePath: String? = null,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val commentCount: Int = 0,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val tags: String? = null, // Comma-separated tags
    val pendingSyncToServer: Boolean = false // For offline-first functionality
) : Parcelable {
    companion object {
        const val CATEGORY_GENERAL = "general"
        const val CATEGORY_TECHNIQUES = "techniques"
        const val CATEGORY_CRITIQUE = "critique"
        const val CATEGORY_EVENTS = "events"
        const val CATEGORY_MARKETPLACE = "marketplace"
        const val CATEGORY_COLLABORATION = "collaboration"
    }
}
