
package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.ForumPost

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum_posts ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPosts(): LiveData<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE category = :category ORDER BY isPinned DESC, createdAt DESC")
    fun getPostsByCategory(category: String): LiveData<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPost): Long

    @Update
    suspend fun updatePost(post: ForumPost)

    @Delete
    suspend fun deletePost(post: ForumPost)

    @Query("SELECT * FROM forum_posts WHERE id = :postId")
    fun getPostById(postId: Long): LiveData<ForumPost>
}
