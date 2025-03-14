package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.artgallery.data.entity.ForumComment
import com.example.artgallery.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying comments in a RecyclerView
 */
class CommentAdapter(
    private val listener: CommentClickListener
) : ListAdapter<ForumComment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the whole item
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val comment = getItem(position)
                    listener.onCommentClick(comment)
                }
            }
        }

        fun bind(comment: ForumComment) {
            binding.textAuthor.text = comment.authorName
            binding.textContent.text = comment.content
            
            // Format and display date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            binding.textDate.text = dateFormat.format(Date(comment.createdAt))
            
            // Show edited indicator if comment was edited
            if (comment.isEdited) {
                binding.textEdited.visibility = View.VISIBLE
            } else {
                binding.textEdited.visibility = View.GONE
            }
            
            // Show reply count if this is a parent comment
            if (comment.parentCommentId == null) {
                binding.textReplyCount.visibility = View.VISIBLE
                binding.textReplyCount.text = "${comment.replyCount} replies"
            } else {
                binding.textReplyCount.visibility = View.GONE
            }
            
            // Apply indentation for replies
            if (comment.parentCommentId != null) {
                val params = binding.cardComment.layoutParams as ViewGroup.MarginLayoutParams
                params.marginStart = 48 // Apply indentation for replies
                binding.cardComment.layoutParams = params
            } else {
                val params = binding.cardComment.layoutParams as ViewGroup.MarginLayoutParams
                params.marginStart = 0 // No indentation for parent comments
                binding.cardComment.layoutParams = params
            }
        }
    }

    interface CommentClickListener {
        fun onCommentClick(comment: ForumComment)
    }

    private class CommentDiffCallback : DiffUtil.ItemCallback<ForumComment>() {
        override fun areItemsTheSame(oldItem: ForumComment, newItem: ForumComment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ForumComment, newItem: ForumComment): Boolean {
            return oldItem == newItem
        }
    }
}
