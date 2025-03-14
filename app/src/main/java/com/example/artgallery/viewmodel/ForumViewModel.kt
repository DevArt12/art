package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.data.entity.ForumComment
import com.example.artgallery.repository.ForumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Community Forum functionality
 */
class ForumViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ForumRepository(application)
    
    // All forum posts
    private val _allPosts = MutableLiveData<List<ForumPost>>(emptyList())
    val allPosts: LiveData<List<ForumPost>> = _allPosts
    
    // Currently selected post
    private val _selectedPost = MutableLiveData<ForumPost?>()
    val selectedPost: LiveData<ForumPost?> = _selectedPost
    
    // Current category filter
    private val _currentCategoryFilter = MutableLiveData<String?>(null)
    val currentCategoryFilter: LiveData<String?> = _currentCategoryFilter
    
    // Filtered posts based on category
    val filteredPosts: LiveData<List<ForumPost>> = _currentCategoryFilter.switchMap { category ->
        when (category) {
            null -> {
                // No filter, show all posts but sort by pinned first, then by date
                _allPosts.map { posts ->
                    posts.sortedWith(
                        compareByDescending<ForumPost> { it.isPinned }
                            .thenByDescending { it.createdAt }
                    )
                }
            }
            else -> {
                // Filter by category and sort
                _allPosts.map { posts ->
                    posts.filter { it.category == category }
                        .sortedWith(
                            compareByDescending<ForumPost> { it.isPinned }
                                .thenByDescending { it.createdAt }
                        )
                }
            }
        }
    }
    
    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // User liked posts (IDs)
    private val _likedPostIds = MutableLiveData<Set<Long>>(emptySet())
    val likedPostIds: LiveData<Set<Long>> = _likedPostIds
    
    init {
        // Load all posts
        refreshPosts()
        
        // Load liked posts
        loadLikedPosts()
    }
    
    /**
     * Refresh the list of forum posts
     */
    fun refreshPosts() {
        viewModelScope.launch {
            try {
                val posts = withContext(Dispatchers.IO) {
                    repository.getAllPostsSync()
                }
                _allPosts.value = posts
            } catch (e: Exception) {
                _errorMessage.value = "Error loading posts: ${e.message}"
            }
        }
    }
    
    /**
     * Set the category filter
     */
    fun setPostCategoryFilter(category: String?) {
        _currentCategoryFilter.value = category
    }
    
    /**
     * Select a post by ID
     */
    fun selectPost(postId: Long) {
        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(postId)
                }
                _selectedPost.value = post
            } catch (e: Exception) {
                _errorMessage.value = "Error selecting post: ${e.message}"
            }
        }
    }
    
    /**
     * Get a post by ID
     */
    fun getPostById(postId: Long): LiveData<ForumPost?> {
        val result = MutableLiveData<ForumPost?>()
        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(postId)
                }
                result.value = post
            } catch (e: Exception) {
                _errorMessage.value = "Error getting post: ${e.message}"
                result.value = null
            }
        }
        return result
    }
    
    /**
     * Create a new forum post
     */
    fun createPost(title: String, content: String, category: String, imagePath: String? = null, tags: String? = null) {
        viewModelScope.launch {
            try {
                // In a real app, we would get the current user ID
                val authorId = 1L // Default user ID for now
                val authorName = "Current User" // Default user name for now
                
                val post = ForumPost(
                    title = title,
                    content = content,
                    authorId = authorId,
                    authorName = authorName,
                    category = category,
                    createdAt = System.currentTimeMillis(),
                    imagePath = imagePath,
                    tags = tags,
                    pendingSyncToServer = true // Mark for syncing when online
                )
                
                val postId = withContext(Dispatchers.IO) {
                    repository.insertPost(post)
                }
                
                // Refresh posts to update UI
                refreshPosts()
                
                // Select the newly created post
                selectPost(postId)
                
            } catch (e: Exception) {
                _errorMessage.value = "Error creating post: ${e.message}"
            }
        }
    }
    
    /**
     * Update an existing forum post
     */
    fun updatePost(postId: Long, title: String, content: String, category: String, imagePath: String? = null, tags: String? = null) {
        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(postId)
                } ?: throw Exception("Post not found")
                
                val updatedPost = post.copy(
                    title = title,
                    content = content,
                    category = category,
                    imagePath = imagePath,
                    tags = tags,
                    lastEditedAt = System.currentTimeMillis(),
                    pendingSyncToServer = true // Mark for syncing when online
                )
                
                withContext(Dispatchers.IO) {
                    repository.updatePost(updatedPost)
                }
                
                // Refresh posts to update UI
                refreshPosts()
                
                // Update selected post
                _selectedPost.value = updatedPost
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating post: ${e.message}"
            }
        }
    }
    
    /**
     * Delete a forum post
     */
    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(postId)
                } ?: throw Exception("Post not found")
                
                withContext(Dispatchers.IO) {
                    repository.deletePost(post)
                }
                
                // Refresh posts to update UI
                refreshPosts()
                
                // Clear selected post if it was deleted
                if (_selectedPost.value?.id == postId) {
                    _selectedPost.value = null
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting post: ${e.message}"
            }
        }
    }
    
    /**
     * Toggle like for a post
     */
    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            try {
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(postId)
                } ?: throw Exception("Post not found")
                
                // Check if post is already liked
                val currentLikedIds = _likedPostIds.value ?: emptySet()
                val isLiked = currentLikedIds.contains(postId)
                
                // Update like count
                val updatedPost = if (isLiked) {
                    // Unlike
                    post.copy(likeCount = post.likeCount - 1)
                } else {
                    // Like
                    post.copy(likeCount = post.likeCount + 1)
                }
                
                // Update post in database
                withContext(Dispatchers.IO) {
                    repository.updatePost(updatedPost)
                }
                
                // Update liked posts set
                val newLikedIds = if (isLiked) {
                    currentLikedIds - postId
                } else {
                    currentLikedIds + postId
                }
                _likedPostIds.value = newLikedIds
                
                // Save liked posts to preferences
                saveLikedPosts(newLikedIds)
                
                // Refresh posts to update UI
                refreshPosts()
                
                // Update selected post if it was liked/unliked
                if (_selectedPost.value?.id == postId) {
                    _selectedPost.value = updatedPost
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating like: ${e.message}"
            }
        }
    }
    
    /**
     * Increment view count for a post
     */
    fun incrementViewCount(postId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.incrementViewCount(postId)
                }
                
                // No need to refresh all posts for this
                // Just update the selected post if it's the one being viewed
                if (_selectedPost.value?.id == postId) {
                    val updatedPost = withContext(Dispatchers.IO) {
                        repository.getPostById(postId)
                    }
                    _selectedPost.value = updatedPost
                }
                
            } catch (e: Exception) {
                // Don't show error for view count failures
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Load liked posts from preferences
     */
    private fun loadLikedPosts() {
        viewModelScope.launch {
            try {
                val likedIds = withContext(Dispatchers.IO) {
                    repository.getLikedPostIds()
                }
                _likedPostIds.value = likedIds
            } catch (e: Exception) {
                e.printStackTrace()
                // Don't show error for this
            }
        }
    }
    
    /**
     * Save liked posts to preferences
     */
    private fun saveLikedPosts(likedIds: Set<Long>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.saveLikedPostIds(likedIds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Don't show error for this
            }
        }
    }
    
    /**
     * Clear the error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Format relative time for display
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> {
                val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                date.format(java.util.Date(timestamp))
            }
        }
    }

    /**
     * Get comments for a post
     */
    fun getCommentsForPost(postId: Long): LiveData<List<ForumComment>> {
        val result = MutableLiveData<List<ForumComment>>()
        viewModelScope.launch {
            try {
                val comments = withContext(Dispatchers.IO) {
                    repository.getCommentsForPost(postId)
                }
                result.value = comments
            } catch (e: Exception) {
                _errorMessage.value = "Error loading comments: ${e.message}"
                result.value = emptyList()
            }
        }
        return result
    }

    /**
     * Add a new comment
     */
    fun addComment(comment: ForumComment): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.insertComment(comment)
                }
                
                // Update comment count for the post
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(comment.postId)
                }
                post?.let {
                    val updatedPost = it.copy(commentCount = it.commentCount + 1)
                    withContext(Dispatchers.IO) {
                        repository.updatePost(updatedPost)
                    }
                }
                
                result.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error adding comment: ${e.message}"
                result.value = false
            }
        }
        return result
    }

    /**
     * Update an existing comment
     */
    fun updateComment(comment: ForumComment) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateComment(comment)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating comment: ${e.message}"
            }
        }
    }

    /**
     * Delete a comment
     */
    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            try {
                val comment = withContext(Dispatchers.IO) {
                    repository.getCommentById(commentId)
                } ?: throw Exception("Comment not found")
                
                withContext(Dispatchers.IO) {
                    repository.deleteComment(comment)
                }
                
                // Update comment count for the post
                val post = withContext(Dispatchers.IO) {
                    repository.getPostById(comment.postId)
                }
                post?.let {
                    val updatedPost = it.copy(commentCount = it.commentCount - 1)
                    withContext(Dispatchers.IO) {
                        repository.updatePost(updatedPost)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting comment: ${e.message}"
            }
        }
    }

    /**
     * Get current user ID (temporary implementation)
     */
    fun getCurrentUserId(): Long {
        // In a real app, this would come from user authentication
        return 1L
    }

    /**
     * Get current user name (temporary implementation)
     */
    fun getCurrentUserName(): String {
        // In a real app, this would come from user authentication
        return "Current User"
    }
}
