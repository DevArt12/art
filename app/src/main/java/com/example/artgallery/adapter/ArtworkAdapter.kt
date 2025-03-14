package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.artgallery.R
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.ItemArtworkBinding
import com.example.artgallery.databinding.ItemLoadingBinding

class ArtworkAdapter(
    private val onItemClick: (Artwork) -> Unit,
    private val onEditClick: (Artwork) -> Unit,
    private val onDeleteClick: (Artwork) -> Unit
) : ListAdapter<ArtworkAdapter.ArtworkItem, RecyclerView.ViewHolder>(ArtworkDiffCallback()) {

    companion object {
        public const val VIEW_TYPE_ARTWORK = 0
        public const val VIEW_TYPE_LOADING = 1
    }

    sealed class ArtworkItem {
        data class ArtworkData(val artwork: Artwork) : ArtworkItem()
        object LoadingItem : ArtworkItem()
    }

    private var isLoadingAdded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ARTWORK -> {
                val binding = ItemArtworkBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ArtworkViewHolder(binding)
            }
            VIEW_TYPE_LOADING -> {
                val binding = ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArtworkViewHolder -> {
                val artworkItem = getItem(position) as ArtworkItem.ArtworkData
                holder.bind(artworkItem.artwork)
            }
            is LoadingViewHolder -> {
                // Nothing to bind for loading view holder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ArtworkItem.ArtworkData -> VIEW_TYPE_ARTWORK
            is ArtworkItem.LoadingItem -> VIEW_TYPE_LOADING
        }
    }

    fun addLoadingFooter() {
        if (!isLoadingAdded) {
            isLoadingAdded = true
            val currentList = currentList.toMutableList()
            currentList.add(ArtworkItem.LoadingItem)
            submitList(currentList)
        }
    }

    fun removeLoadingFooter() {
        if (isLoadingAdded) {
            isLoadingAdded = false
            val currentList = currentList.toMutableList()
            val position = currentList.lastIndex
            if (position >= 0 && getItem(position) is ArtworkItem.LoadingItem) {
                currentList.removeAt(position)
                submitList(currentList)
            }
        }
    }

    fun submitArtworkList(artworks: List<Artwork>) {
        val items = artworks.map { ArtworkItem.ArtworkData(it) }
        val newList = if (isLoadingAdded) {
            items + ArtworkItem.LoadingItem
        } else {
            items
        }
        submitList(newList)
    }

    inner class ArtworkViewHolder(
        private val binding: ItemArtworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position) as? ArtworkItem.ArtworkData ?: return@setOnClickListener
                    onItemClick(item.artwork)
                }
            }

            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position) as? ArtworkItem.ArtworkData ?: return@setOnClickListener
                    onEditClick(item.artwork)
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position) as? ArtworkItem.ArtworkData ?: return@setOnClickListener
                    onDeleteClick(item.artwork)
                }
            }
        }

        fun bind(artwork: Artwork) {
            binding.apply {
                artworkTitle.text = artwork.title
                artworkDescription.text = artwork.description
                artworkPrice.text = artwork.getFormattedPrice()
                
                // Show category if available
                if (artwork.category.isNullOrBlank()) {
                    artworkCategory.visibility = View.GONE
                } else {
                    artworkCategory.visibility = View.VISIBLE
                    artworkCategory.text = artwork.category
                }
                
                // Show "For Sale" badge if applicable
                forSaleBadge.visibility = if (artwork.isForSale) View.VISIBLE else View.GONE

                // Load image with placeholder and error handling
                Glide.with(root.context)
                    .load(artwork.imagePath)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .centerCrop()
                    .into(artworkImage)
            }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

    private class ArtworkDiffCallback : DiffUtil.ItemCallback<ArtworkItem>() {
        override fun areItemsTheSame(oldItem: ArtworkItem, newItem: ArtworkItem): Boolean {
            return when {
                oldItem is ArtworkItem.ArtworkData && newItem is ArtworkItem.ArtworkData ->
                    oldItem.artwork.id == newItem.artwork.id
                oldItem is ArtworkItem.LoadingItem && newItem is ArtworkItem.LoadingItem -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ArtworkItem, newItem: ArtworkItem): Boolean {
            return when {
                oldItem is ArtworkItem.ArtworkData && newItem is ArtworkItem.ArtworkData ->
                    oldItem.artwork == newItem.artwork
                oldItem is ArtworkItem.LoadingItem && newItem is ArtworkItem.LoadingItem -> true
                else -> false
            }
        }
    }
}
