package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.databinding.ItemArModelBinding
import com.example.artgallery.data.entity.ARModel

/**
 * Adapter for displaying AR models in a RecyclerView
 */
class ARModelAdapter(
    private val listener: ARModelClickListener
) : ListAdapter<ARModel, ARModelAdapter.ARModelViewHolder>(ARModelDiffCallback()) {

    // Map to store download progress for each model
    private val downloadProgressMap = mutableMapOf<Long, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ARModelViewHolder {
        val binding = ItemArModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ARModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ARModelViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model, downloadProgressMap[model.id] ?: 0)
    }

    /**
     * Update the download progress for a specific model
     */
    fun updateDownloadProgress(modelId: Long, progress: Int) {
        downloadProgressMap[modelId] = progress
        
        // Find the position of the model in the current list
        val position = currentList.indexOfFirst { it.id == modelId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class ARModelViewHolder(
        private val binding: ItemArModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the entire item
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val model = getItem(position)
                    listener.onViewModelClick(model)
                }
            }

            // Set click listener for the download button
            binding.buttonDownload.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val model = getItem(position)
                    listener.onDownloadClick(model)
                }
            }
        }

        fun bind(model: ARModel, downloadProgress: Int) {
            // Bind model data to views
            binding.textModelName.text = model.name
            binding.textModelCategory.text = model.category.capitalize()
            
            // Set description if available
            if (binding.textModelDescription != null && model.description != null) {
                binding.textModelDescription.text = model.description
            }

            // Load thumbnail image
            Glide.with(binding.root.context)
                .load(model.thumbnailPath)
                .placeholder(R.drawable.placeholder_ar_model)
                .error(R.drawable.placeholder_ar_model)
                .into(binding.imageModelThumbnail)

            // Update download button state based on download status
            if (model.isDownloaded) {
                // Model is already downloaded
                binding.buttonDownload.text = binding.root.context.getString(R.string.view_in_ar)
                binding.buttonDownload.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.purple_500)
                )
                binding.progressDownload.visibility = View.GONE
            } else if (downloadProgress > 0 && downloadProgress < 100) {
                // Download in progress
                binding.buttonDownload.text = "$downloadProgress%"
                binding.buttonDownload.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorAccent)
                )
                binding.progressDownload.visibility = View.VISIBLE
                binding.progressDownload.progress = downloadProgress
            } else {
                // Not downloaded yet
                binding.buttonDownload.text = binding.root.context.getString(R.string.download)
                binding.buttonDownload.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.colorAccent)
                )
                binding.progressDownload.visibility = View.GONE
            }

            // Show file size if available
            if (binding.textFileSize != null && model.fileSize > 0) {
                binding.textFileSize.visibility = View.VISIBLE
                binding.textFileSize.text = formatFileSize(model.fileSize)
            } else if (binding.textFileSize != null) {
                binding.textFileSize.visibility = View.GONE
            }
        }
        
        // Simple file size formatter
        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }
        }
    }

    /**
     * Interface for handling AR model interactions
     */
    interface ARModelClickListener {
        fun onViewModelClick(model: ARModel)
        fun onDownloadClick(model: ARModel)
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    class ARModelDiffCallback : DiffUtil.ItemCallback<ARModel>() {
        override fun areItemsTheSame(oldItem: ARModel, newItem: ARModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ARModel, newItem: ARModel): Boolean {
            return oldItem == newItem
        }
    }
}

// Extension function to capitalize first letter of a string
private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase() else it.toString() 
    }
}
