package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.databinding.ItemTutorialBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying tutorials in a RecyclerView
 */
class TutorialAdapter(
    private val listener: TutorialClickListener
) : ListAdapter<Tutorial, TutorialAdapter.TutorialViewHolder>(TutorialDiffCallback()) {

    // Map of tutorial IDs to download progress
    private val downloadProgressMap = mutableMapOf<Long, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val binding = ItemTutorialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        val tutorial = getItem(position)
        val downloadProgress = downloadProgressMap[tutorial.id]
        holder.bind(tutorial, downloadProgress)
    }

    /**
     * Update download progress for a tutorial
     */
    fun updateDownloadProgress(tutorialId: Long, progress: Int) {
        downloadProgressMap[tutorialId] = progress
        
        // Find the position of the tutorial in the list
        val position = currentList.indexOfFirst { it.id == tutorialId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class TutorialViewHolder(
        private val binding: ItemTutorialBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for the whole item
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val tutorial = getItem(position)
                    listener.onTutorialClick(tutorial)
                }
            }

            // Set click listener for download button
            binding.buttonDownload.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val tutorial = getItem(position)
                    listener.onDownloadClick(tutorial)
                }
            }
        }

        fun bind(tutorial: Tutorial, downloadProgress: Int?) {
            binding.textTitle.text = tutorial.title
            binding.textDescription.text = tutorial.description
            binding.textCategory.text = tutorial.category.capitalize()
            binding.chipDifficulty.text = tutorial.difficulty.capitalize()
            
            // Format and display duration
            val hours = tutorial.duration / 3600
            val minutes = (tutorial.duration % 3600) / 60
            val seconds = tutorial.duration % 60
            
            val formattedDuration = when {
                hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
                else -> String.format("%d:%02d", minutes, seconds)
            }
            binding.textDuration.text = formattedDuration
            
            // Set difficulty chip color based on level
            val difficultyColorRes = when (tutorial.difficulty) {
                Tutorial.DIFFICULTY_BEGINNER -> R.color.colorBeginner
                Tutorial.DIFFICULTY_INTERMEDIATE -> R.color.colorIntermediate
                Tutorial.DIFFICULTY_ADVANCED -> R.color.colorAdvanced
                else -> R.color.colorPrimary
            }
            binding.chipDifficulty.setChipBackgroundColorResource(difficultyColorRes)
            
            // Show progress if user has started the tutorial
            if (tutorial.userProgress > 0) {
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.progress = tutorial.userProgress
                binding.textProgress.visibility = View.VISIBLE
                binding.textProgress.text = "${tutorial.userProgress}% completed"
            } else {
                binding.progressBar.visibility = View.GONE
                binding.textProgress.visibility = View.GONE
            }
            
            // Show completed badge if quiz is completed
            binding.imageBadgeCompleted.visibility = if (tutorial.hasCompletedQuiz) View.VISIBLE else View.GONE
            
            // Load thumbnail
            val thumbnailFile = File(tutorial.thumbnailPath)
            if (thumbnailFile.exists()) {
                // Load from local file
                Glide.with(binding.imageThumbnail)
                    .load(thumbnailFile)
                    .placeholder(R.drawable.placeholder_tutorial)
                    .error(R.drawable.placeholder_tutorial)
                    .centerCrop()
                    .into(binding.imageThumbnail)
            } else {
                // Load from resource or URL
                Glide.with(binding.imageThumbnail)
                    .load(tutorial.thumbnailPath)
                    .placeholder(R.drawable.placeholder_tutorial)
                    .error(R.drawable.placeholder_tutorial)
                    .centerCrop()
                    .into(binding.imageThumbnail)
            }
            
            // Update download button state
            if (tutorial.isDownloaded) {
                binding.buttonDownload.setIconResource(R.drawable.ic_downloaded)
                binding.buttonDownload.setText(R.string.downloaded)
                binding.downloadProgressBar.visibility = View.GONE
            } else if (downloadProgress != null && downloadProgress < 100) {
                binding.buttonDownload.text = "$downloadProgress%"
                binding.buttonDownload.isEnabled = false
                binding.downloadProgressBar.visibility = View.VISIBLE
                binding.downloadProgressBar.progress = downloadProgress
            } else {
                binding.buttonDownload.setIconResource(R.drawable.ic_download)
                binding.buttonDownload.setText(R.string.download)
                binding.buttonDownload.isEnabled = true
                binding.downloadProgressBar.visibility = View.GONE
            }
            
            // Format and display date added
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.textDateAdded.text = "Added ${dateFormat.format(Date(tutorial.dateAdded))}"
            
            // Show materials needed if available
            if (tutorial.materialsNeeded.isNullOrBlank()) {
                binding.textMaterials.visibility = View.GONE
            } else {
                binding.textMaterials.visibility = View.VISIBLE
                binding.textMaterials.text = "Materials: ${tutorial.materialsNeeded}"
            }
        }
    }

    interface TutorialClickListener {
        fun onTutorialClick(tutorial: Tutorial)
        fun onDownloadClick(tutorial: Tutorial)
    }

    private class TutorialDiffCallback : DiffUtil.ItemCallback<Tutorial>() {
        override fun areItemsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tutorial, newItem: Tutorial): Boolean {
            return oldItem == newItem
        }
    }

    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
