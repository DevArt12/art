package com.example.artgallery.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.ForumComment
import com.example.artgallery.data.entity.ForumPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for managing forum posts and comments, including database operations
 */
class ForumRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val forumPostDao = database.forumPostDao()
    private val forumCommentDao = database.forumCommentDao()
    private val preferences: SharedPreferences = context.getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
    
    // Get all forum posts from the database
    fun getAllPosts(): LiveData<List<ForumPost>> {
        return forumPostDao.getAllPosts()
    }
    
    // Get all forum posts synchronously (non-LiveData)
    suspend fun getAllPostsSync(): List<ForumPost> {
        return forumPostDao.getAllPostsSync()
    }
    
    // Get forum posts by category
    fun getPostsByCategory(category: String): LiveData<List<ForumPost>> {
        return forumPostDao.getPostsByCategory(category)
    }
    
    // Get forum posts by author
    fun getPostsByAuthor(authorId: Long): LiveData<List<ForumPost>> {
        return forumPostDao.getPostsByAuthor(authorId)
    }
    
    // Get forum post by ID
    suspend fun getPostById(postId: Long): ForumPost? {
        return forumPostDao.getPostById(postId)
    }
    
    // Insert a new forum post
    suspend fun insertPost(post: ForumPost): Long {
        return forumPostDao.insertPost(post)
    }
    
    // Update an existing forum post
    suspend fun updatePost(post: ForumPost) {
        forumPostDao.updatePost(post)
    }
    
    // Delete a forum post
    suspend fun deletePost(post: ForumPost) {
        // Delete all comments for this post first
        deleteCommentsForPost(post.id)
        
        // Then delete the post itself
        forumPostDao.deletePost(post)
        
        // Delete any associated image file
        post.imagePath?.let { path ->
            val imageFile = File(path)
            if (imageFile.exists()) {
                imageFile.delete()
            }
        }
    }
    
    // Delete all comments for a post
    suspend fun deleteCommentsForPost(postId: Long) {
        forumCommentDao.deleteAllCommentsForPost(postId)
    }
    
    // Increment view count for a post
    suspend fun incrementViewCount(postId: Long) {
        val post = forumPostDao.getPostById(postId) ?: return
        val updatedPost = post.copy(viewCount = post.viewCount + 1)
        forumPostDao.updatePost(updatedPost)
    }
    
    // Increment comment count for a post
    suspend fun incrementCommentCount(postId: Long) {
        val post = forumPostDao.getPostById(postId) ?: return
        val updatedPost = post.copy(commentCount = post.commentCount + 1)
        forumPostDao.updatePost(updatedPost)
    }
    
    // Decrement comment count for a post
    suspend fun decrementCommentCount(postId: Long) {
        val post = forumPostDao.getPostById(postId) ?: return
        val updatedPost = post.copy(commentCount = Math.max(0, post.commentCount - 1))
        forumPostDao.updatePost(updatedPost)
    }
    
    // Get comments for a post
    fun getCommentsForPost(postId: Long): LiveData<List<ForumComment>> {
        return forumCommentDao.getCommentsForPost(postId)
    }
    
    // Get comments for a post synchronously
    suspend fun getCommentsForPostSync(postId: Long): List<ForumComment> {
        return forumCommentDao.getCommentsForPostSync(postId)
    }
    
    // Insert a new comment
    suspend fun insertComment(comment: ForumComment): Long {
        val commentId = forumCommentDao.insertComment(comment)
        
        // Increment comment count on the post
        incrementCommentCount(comment.postId)
        
        return commentId
    }
    
    // Update an existing comment
    suspend fun updateComment(comment: ForumComment) {
        forumCommentDao.updateComment(comment)
    }
    
    // Delete a comment
    suspend fun deleteComment(comment: ForumComment) {
        forumCommentDao.deleteComment(comment)
        
        // Decrement comment count on the post
        decrementCommentCount(comment.postId)
    }
    
    // Get liked post IDs from preferences
    suspend fun getLikedPostIds(): Set<Long> {
        return withContext(Dispatchers.IO) {
            val likedIdsString = preferences.getStringSet("liked_post_ids", emptySet()) ?: emptySet()
            likedIdsString.mapNotNull { it.toLongOrNull() }.toSet()
        }
    }
    
    // Save liked post IDs to preferences
    suspend fun saveLikedPostIds(likedIds: Set<Long>) {
        withContext(Dispatchers.IO) {
            val likedIdsString = likedIds.map { it.toString() }.toSet()
            preferences.edit().putStringSet("liked_post_ids", likedIdsString).apply()
        }
    }
    
    // Get posts pending sync to server
    suspend fun getPostsPendingSync(): List<ForumPost> {
        return forumPostDao.getPostsPendingSync()
    }
    
    // Get comments pending sync to server
    suspend fun getCommentsPendingSync(): List<ForumComment> {
        return forumCommentDao.getCommentsPendingSync()
    }
    
    // Mark post as synced
    suspend fun markPostAsSynced(postId: Long) {
        val post = forumPostDao.getPostById(postId) ?: return
        val updatedPost = post.copy(pendingSyncToServer = false)
        forumPostDao.updatePost(updatedPost)
    }
    
    // Mark comment as synced
    suspend fun markCommentAsSynced(commentId: Long) {
        val comment = forumCommentDao.getCommentById(commentId) ?: return
        val updatedComment = comment.copy(pendingSyncToServer = false)
        forumCommentDao.updateComment(updatedComment)
    }
    
    // Search posts by query
    suspend fun searchPosts(query: String): List<ForumPost> {
        return forumPostDao.searchPostsSync(query)
    }
}
