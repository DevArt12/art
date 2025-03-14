package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.data.entity.ArtNews
import com.example.artgallery.databinding.ItemArtNewsBinding
import java.text.SimpleDateFormat
import java.util.*

class ArtNewsAdapter(
    private val onItemClick: (ArtNews) -> Unit,
    private val onEditClick: (ArtNews) -> Unit,
    private val onDeleteClick: (ArtNews) -> Unit
) : ListAdapter<ArtNews, ArtNewsAdapter.ArtNewsViewHolder>(ArtNewsDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtNewsViewHolder {
        val binding = ItemArtNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtNewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtNewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtNewsViewHolder(
        private val binding: ItemArtNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(news: ArtNews) {
            binding.apply {
                tvTitle.text = news.title
                tvContent.text = news.content
                tvDate.text = dateFormat.format(Date(news.date))

                news.imagePath?.let { path ->
                    Glide.with(root.context)
                        .load(path)
                        .centerCrop()
                        .into(ivNews)
                }
            }
        }
    }

    private class ArtNewsDiffCallback : DiffUtil.ItemCallback<ArtNews>() {
        override fun areItemsTheSame(oldItem: ArtNews, newItem: ArtNews): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArtNews, newItem: ArtNews): Boolean {
            return oldItem == newItem
        }
    }
}
