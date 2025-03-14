package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "forum_comments",
    foreignKeys = [
        ForeignKey(
            entity = ForumPost::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("postId"), Index("authorId"), Index("parentCommentId")]
)
data class ForumComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val content: String,
    val authorId: Long? = null, // Can be null if user is deleted but comment remains
    val authorName: String, // Store name separately in case user is deleted
    val createdAt: Long,
    val lastEditedAt: Long? = null,
    val imagePath: String? = null,
    val likeCount: Int = 0,
    val parentCommentId: Long? = null, // For nested comments/replies
    val isEdited: Boolean = false,
    val pendingSyncToServer: Boolean = false, // For offline-first functionality
    val replyCount: Int = 0 // Count of replies to this comment
) : Parcelable
