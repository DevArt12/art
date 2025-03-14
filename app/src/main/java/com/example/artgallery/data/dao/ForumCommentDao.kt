package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.ForumComment

@Dao
interface ForumCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ForumComment): Long

    @Update
    suspend fun updateComment(comment: ForumComment)

    @Delete
    suspend fun deleteComment(comment: ForumComment)

    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Long): LiveData<List<ForumComment>>

    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY createdAt ASC")
    suspend fun getCommentsForPostSync(postId: Long): List<ForumComment>

    @Query("SELECT * FROM forum_comments WHERE id = :commentId")
    suspend fun getCommentById(commentId: Long): ForumComment?

    @Query("SELECT * FROM forum_comments WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getCommentsByAuthor(authorId: Long): LiveData<List<ForumComment>>

    @Query("SELECT * FROM forum_comments WHERE parentCommentId = :parentCommentId ORDER BY createdAt ASC")
    fun getRepliesForComment(parentCommentId: Long): LiveData<List<ForumComment>>

    @Query("UPDATE forum_comments SET likeCount = likeCount + 1 WHERE id = :commentId")
    suspend fun incrementLikeCount(commentId: Long)

    @Query("UPDATE forum_comments SET likeCount = likeCount - 1 WHERE id = :commentId AND likeCount > 0")
    suspend fun decrementLikeCount(commentId: Long)

    @Query("SELECT * FROM forum_comments WHERE pendingSyncToServer = 1")
    suspend fun getCommentsPendingSync(): List<ForumComment>

    @Query("UPDATE forum_comments SET pendingSyncToServer = 0 WHERE id = :commentId")
    suspend fun markCommentSynced(commentId: Long)

    @Query("SELECT COUNT(*) FROM forum_comments WHERE postId = :postId")
    suspend fun getCommentCountForPost(postId: Long): Int

    @Query("DELETE FROM forum_comments WHERE postId = :postId")
    suspend fun deleteAllCommentsForPost(postId: Long)
}
