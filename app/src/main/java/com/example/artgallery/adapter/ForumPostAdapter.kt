package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.databinding.ItemForumPostBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying forum posts in a RecyclerView
 */
class ForumPostAdapter(
    private val listener: ForumPostClickListener
) : ListAdapter<ForumPost, ForumPostAdapter.ForumPostViewHolder>(ForumPostDiffCallback()) {

    // Set of post IDs that the user has liked
    private var likedPostIds: Set<Long> = emptySet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumPostViewHolder {
        val binding = ItemForumPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForumPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumPostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post, likedPostIds.contains(post.id))
    }

    /**
     * Update the set of liked post IDs
     */
    fun updateLikedPosts(likedIds: Set<Long>) {
        likedPostIds = likedIds
        notifyDataSetChanged() // This is inefficient, but simplest for now
    }

    inner class ForumPostViewHolder(
        private val binding: ItemForumPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the whole item
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = getItem(position)
                    listener.onPostClick(post)
                }
            }

            // Set click listener for like button
            binding.buttonLike.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val post = getItem(position)
                    listener.onLikeClick(post)
                }
            }
        }

        fun bind(post: ForumPost, isLiked: Boolean) {
            binding.textTitle.text = post.title
            binding.textContent.text = post.content
            binding.textAuthor.text = post.authorName
            binding.textCategory.text = post.category.capitalize()
            binding.textCommentCount.text = "${post.commentCount}"
            binding.buttonLike.text = "${post.likeCount}"
            binding.textViewCount.text = "${post.viewCount}"
            
            // Format and display date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.textDate.text = dateFormat.format(Date(post.createdAt))

            // Show pinned indicator if post is pinned
            binding.imagePinned.visibility = if (post.isPinned) View.VISIBLE else View.GONE

            // Load post image if available
            if (post.imagePath != null && post.imagePath.isNotEmpty()) {
                binding.imagePost.visibility = View.VISIBLE
                val imageFile = File(post.imagePath)
                if (imageFile.exists()) {
                    // Load from local file
                    Glide.with(binding.imagePost)
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_post_image)
                        .error(R.drawable.placeholder_post_image)
                        .centerCrop()
                        .into(binding.imagePost)
                } else {
                    // Load from URL or resource
                    Glide.with(binding.imagePost)
                        .load(post.imagePath)
                        .placeholder(R.drawable.placeholder_post_image)
                        .error(R.drawable.placeholder_post_image)
                        .centerCrop()
                        .into(binding.imagePost)
                }
            } else {
                binding.imagePost.visibility = View.GONE
            }

            // Update like button state
            if (isLiked) {
                binding.buttonLike.setIconResource(R.drawable.ic_favorite)
                binding.buttonLike.setIconTintResource(R.color.colorLiked)
            } else {
                binding.buttonLike.setIconResource(R.drawable.ic_favorite_border)
                binding.buttonLike.setIconTintResource(R.color.colorUnliked)
            }

            // Show tags if available
            if (post.tags != null && post.tags.isNotEmpty()) {
                binding.textTags.visibility = View.VISIBLE
                binding.textTags.text = post.tags.replace(",", " • ")
            } else {
                binding.textTags.visibility = View.GONE
            }
        }
    }

    interface ForumPostClickListener {
        fun onPostClick(post: ForumPost)
        fun onLikeClick(post: ForumPost)
    }

    private class ForumPostDiffCallback : DiffUtil.ItemCallback<ForumPost>() {
        override fun areItemsTheSame(oldItem: ForumPost, newItem: ForumPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ForumPost, newItem: ForumPost): Boolean {
            return oldItem == newItem
        }
    }

    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.databinding.ItemForumPostBinding
import java.text.SimpleDateFormat
import java.util.*

class ForumPostAdapter(
    private val onPostClick: (ForumPost) -> Unit
) : ListAdapter<ForumPost, ForumPostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            ItemForumPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemForumPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: ForumPost) {
            binding.apply {
                tvTitle.text = post.title
                tvAuthor.text = post.authorName
                tvDate.text = formatDate(post.createdAt)
                tvCategory.text = post.category
                root.setOnClickListener { onPostClick(post) }
            }
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<ForumPost>() {
        override fun areItemsTheSame(oldItem: ForumPost, newItem: ForumPost) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ForumPost, newItem: ForumPost) = 
            oldItem == newItem
    }
}
