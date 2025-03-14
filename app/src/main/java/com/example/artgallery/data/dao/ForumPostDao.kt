package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.ForumPost

@Dao
interface ForumPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPost): Long

    @Update
    suspend fun updatePost(post: ForumPost)

    @Delete
    suspend fun deletePost(post: ForumPost)

    @Query("SELECT * FROM forum_posts ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPosts(): LiveData<List<ForumPost>>

    @Query("SELECT * FROM forum_posts ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAllPostsSync(): List<ForumPost>

    @Query("SELECT * FROM forum_posts WHERE id = :postId")
    suspend fun getPostById(postId: Long): ForumPost?

    @Query("SELECT * FROM forum_posts WHERE category = :category ORDER BY isPinned DESC, createdAt DESC")
    fun getPostsByCategory(category: String): LiveData<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: Long): LiveData<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPosts(query: String): LiveData<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchPostsSync(query: String): List<ForumPost>

    @Query("UPDATE forum_posts SET likeCount = likeCount + 1 WHERE id = :postId")
    suspend fun incrementLikeCount(postId: Long)

    @Query("UPDATE forum_posts SET likeCount = likeCount - 1 WHERE id = :postId AND likeCount > 0")
    suspend fun decrementLikeCount(postId: Long)

    @Query("UPDATE forum_posts SET viewCount = viewCount + 1 WHERE id = :postId")
    suspend fun incrementViewCount(postId: Long)

    @Query("UPDATE forum_posts SET commentCount = commentCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: Long)

    @Query("UPDATE forum_posts SET commentCount = commentCount - 1 WHERE id = :postId AND commentCount > 0")
    suspend fun decrementCommentCount(postId: Long)

    @Query("UPDATE forum_posts SET isPinned = :isPinned WHERE id = :postId")
    suspend fun updatePinStatus(postId: Long, isPinned: Boolean)

    @Query("UPDATE forum_posts SET isLocked = :isLocked WHERE id = :postId")
    suspend fun updateLockStatus(postId: Long, isLocked: Boolean)

    @Query("SELECT * FROM forum_posts WHERE pendingSyncToServer = 1")
    suspend fun getPostsPendingSync(): List<ForumPost>

    @Query("UPDATE forum_posts SET pendingSyncToServer = 0 WHERE id = :postId")
    suspend fun markPostSynced(postId: Long)
}
