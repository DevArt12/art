package com.example.artgallery.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.artgallery.adapter.ArtworkAdapter
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.FragmentMarketBinding
import com.example.artgallery.model.ArtworkFilter
import com.example.artgallery.ui.dialog.ArtworkDetailsDialogFragment
import com.example.artgallery.ui.dialog.ArtworkEditDialogFragment
import com.example.artgallery.ui.gallery.FilterBottomSheetFragment
import com.example.artgallery.viewmodel.ArtworkViewModel

class MarketFragment : Fragment() {
    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtworkViewModel by viewModels()
    private lateinit var artworkAdapter: ArtworkAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterButton()
        observeArtworks()
    }

    private fun setupRecyclerView() {
        artworkAdapter = ArtworkAdapter(
            onItemClick = { artwork ->
                // Show artwork details
                ArtworkDetailsDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "artwork_details")
            },
            onEditClick = { artwork ->
                // Show edit dialog
                ArtworkEditDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "edit_artwork")
            },
            onDeleteClick = { artwork ->
                // Delete artwork
                viewModel.deleteArtwork(artwork)
            }
        )

        binding.rvArtworks.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = artworkAdapter
        }
    }

    private fun setupFilterButton() {
        binding.fabFilter.setOnClickListener {
            FilterBottomSheetFragment { filter ->
                viewModel.applyFilter(filter)
            }.show(childFragmentManager, "FilterBottomSheet")
        }
    }

    private fun observeArtworks() {
        viewModel.filteredArtworks.observe(viewLifecycleOwner) { artworks ->
            artworkAdapter.submitList(artworks.filter { it.isForSale })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
