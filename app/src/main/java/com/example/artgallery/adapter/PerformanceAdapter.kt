package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.data.entity.Performance
import com.example.artgallery.databinding.ItemPerformanceBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying performances in a RecyclerView
 */
class PerformanceAdapter(
    private val listener: PerformanceClickListener
) : ListAdapter<Performance, PerformanceAdapter.PerformanceViewHolder>(PerformanceDiffCallback()) {

    // Map to track download progress for each performance
    private val downloadProgressMap = mutableMapOf<Long, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceViewHolder {
        val binding = ItemPerformanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PerformanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PerformanceViewHolder, position: Int) {
        val performance = getItem(position)
        holder.bind(performance, downloadProgressMap[performance.id] ?: 0)
    }

    /**
     * Update download progress for a specific performance
     */
    fun updateDownloadProgress(performanceId: Long, progress: Int) {
        downloadProgressMap[performanceId] = progress
        notifyItemChanged(currentList.indexOfFirst { it.id == performanceId })
    }

    inner class PerformanceViewHolder(
        private val binding: ItemPerformanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the whole item
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val performance = getItem(position)
                    listener.onPerformanceClick(performance)
                }
            }

            // Set click listener for download button
            binding.buttonDownload.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val performance = getItem(position)
                    listener.onDownloadClick(performance)
                }
            }
        }

        fun bind(performance: Performance, downloadProgress: Int) {
            binding.textTitle.text = performance.title
            binding.textCategory.text = performance.category.capitalize()
            binding.textDuration.text = formatDuration(performance.duration)
            binding.textViewCount.text = "${performance.viewCount} views"
            
            // Format and display date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.textDate.text = dateFormat.format(Date(performance.dateRecorded))

            // Load thumbnail
            if (performance.thumbnailPath.isNotEmpty()) {
                val thumbnailFile = File(performance.thumbnailPath)
                if (thumbnailFile.exists()) {
                    // Load from local file
                    Glide.with(binding.imageThumbnail)
                        .load(thumbnailFile)
                        .placeholder(R.drawable.placeholder_performance)
                        .error(R.drawable.placeholder_performance)
                        .centerCrop()
                        .into(binding.imageThumbnail)
                } else {
                    // Load from URL or resource
                    Glide.with(binding.imageThumbnail)
                        .load(performance.thumbnailPath)
                        .placeholder(R.drawable.placeholder_performance)
                        .error(R.drawable.placeholder_performance)
                        .centerCrop()
                        .into(binding.imageThumbnail)
                }
            } else {
                // Use placeholder
                Glide.with(binding.imageThumbnail)
                    .load(R.drawable.placeholder_performance)
                    .centerCrop()
                    .into(binding.imageThumbnail)
            }

            // Update download button state
            if (performance.isDownloaded) {
                binding.buttonDownload.setIconResource(R.drawable.ic_play)
                binding.buttonDownload.setText(R.string.play)
                binding.progressDownload.visibility = View.GONE
            } else {
                binding.buttonDownload.setIconResource(R.drawable.ic_download)
                binding.buttonDownload.setText(R.string.download)
                
                // Show progress if download is in progress
                if (downloadProgress > 0 && downloadProgress < 100) {
                    binding.progressDownload.visibility = View.VISIBLE
                    binding.progressDownload.progress = downloadProgress
                } else {
                    binding.progressDownload.visibility = View.GONE
                }
            }
        }

        private fun formatDuration(durationInSeconds: Int): String {
            val minutes = durationInSeconds / 60
            val seconds = durationInSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    interface PerformanceClickListener {
        fun onPerformanceClick(performance: Performance)
        fun onDownloadClick(performance: Performance)
    }

    private class PerformanceDiffCallback : DiffUtil.ItemCallback<Performance>() {
        override fun areItemsTheSame(oldItem: Performance, newItem: Performance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Performance, newItem: Performance): Boolean {
            return oldItem == newItem
        }
    }

    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
