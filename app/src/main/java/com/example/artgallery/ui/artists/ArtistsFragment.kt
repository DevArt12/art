package com.example.artgallery.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.artgallery.adapter.ArtistAdapter
import com.example.artgallery.databinding.FragmentArtistsBinding
import com.example.artgallery.viewmodel.ArtistViewModel
import com.example.artgallery.data.entity.Artist
import com.example.artgallery.ui.dialog.ArtistDialogFragment
import com.example.artgallery.utils.AnimationUtils

class ArtistsFragment : Fragment() {
    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtistViewModel by viewModels()
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFab()
        observeArtists()
        
        // Add entrance animation for the RecyclerView
        AnimationUtils.fadeIn(binding.recyclerView)
        
        // Add scale animation for FAB
        AnimationUtils.scaleUp(binding.fabAdd)
    }

    private fun setupRecyclerView() {
        artistAdapter = ArtistAdapter(
            onItemClick = { artist -> 
                showArtistDetails(artist)
            },
            onEditClick = { artist ->
                showEditDialog(artist)
            },
            onDeleteClick = { artist ->
                deleteArtist(artist)
            }
        )
        
        binding.recyclerView.apply {
            adapter = artistAdapter
            layoutManager = LinearLayoutManager(context)
            // Add item animations
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
                moveDuration = 300
                changeDuration = 300
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { view ->
            // Add pulse animation when clicking FAB
            AnimationUtils.pulseAnimation(view)
            showAddDialog()
        }
    }

    private fun observeArtists() {
        viewModel.allArtists.observe(viewLifecycleOwner) { artists ->
            artistAdapter.submitList(artists)
        }
    }

    private fun showArtistDetails(artist: Artist) {
        // Show artist details
    }

    private fun showEditDialog(artist: Artist) {
        val dialog = ArtistDialogFragment.newInstance(artist)
        dialog.show(childFragmentManager, "edit_artist")
    }

    private fun deleteArtist(artist: Artist) {
        viewModel.deleteArtist(artist)
    }

    private fun showAddDialog() {
        val dialog = ArtistDialogFragment.newInstance(null)
        dialog.show(childFragmentManager, "add_artist")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
