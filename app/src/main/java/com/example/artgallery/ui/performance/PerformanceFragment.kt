package com.example.artgallery.ui.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artgallery.R
import com.example.artgallery.adapter.PerformanceAdapter
import com.example.artgallery.data.entity.Performance
import com.example.artgallery.databinding.FragmentPerformanceBinding
import com.example.artgallery.viewmodel.PerformanceViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for browsing and watching performances
 */
class PerformanceFragment : Fragment(), PerformanceAdapter.PerformanceClickListener {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerformanceViewModel by viewModels()
    private lateinit var adapter: PerformanceAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilters()
        observeViewModel()
        
        // Set up swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPerformances()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = PerformanceAdapter(this)
        binding.recyclerViewPerformances.adapter = adapter
        binding.recyclerViewPerformances.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupCategoryFilters() {
        // Set up chip click listeners
        val chipClickListener = View.OnClickListener { view ->
            val chip = view as Chip
            
            // Set the category filter in the ViewModel
            val category = when (chip.id) {
                R.id.chip_all -> null // null means no filter (all performances)
                R.id.chip_downloaded -> "downloaded"
                R.id.chip_music -> Performance.CATEGORY_MUSIC
                R.id.chip_dance -> Performance.CATEGORY_DANCE
                R.id.chip_theater -> Performance.CATEGORY_THEATER
                R.id.chip_mixed_media -> Performance.CATEGORY_MIXED_MEDIA
                else -> null
            }
            
            viewModel.setPerformanceCategoryFilter(category)
        }
        
        // Apply the click listener to all chips
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_downloaded).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_music).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_dance).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_theater).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_mixed_media).setOnClickListener(chipClickListener)
        
        // Select "All" by default
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).isChecked = true
    }
    
    private fun observeViewModel() {
        // Observe filtered performances
        viewModel.filteredPerformances.observe(viewLifecycleOwner, Observer { performances ->
            adapter.submitList(performances)
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Show/hide empty state
            if (performances.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewPerformances.visibility = View.GONE
                
                // Update empty state message based on current filter
                viewModel.currentCategoryFilter.value?.let { category ->
                    when (category) {
                        "downloaded" -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_downloaded_performances)
                            binding.textEmptyStateMessage.text = getString(R.string.download_performances_message)
                        }
                        null -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_performances_available)
                            binding.textEmptyStateMessage.text = getString(R.string.check_back_later)
                        }
                        else -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_performances_in_category, category.capitalize())
                            binding.textEmptyStateMessage.text = getString(R.string.try_different_category)
                        }
                    }
                }
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewPerformances.visibility = View.VISIBLE
            }
        })
        
        // Observe download progress
        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is PerformanceViewModel.DownloadStatus.InProgress -> {
                    adapter.updateDownloadProgress(status.performanceId, status.progress)
                }
                is PerformanceViewModel.DownloadStatus.Success -> {
                    adapter.updateDownloadProgress(status.performanceId, 100)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.download_complete),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is PerformanceViewModel.DownloadStatus.Failed -> {
                    adapter.updateDownloadProgress(status.performanceId, 0)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.download_failed, status.error),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        })
    }
    
    override fun onPerformanceClick(performance: Performance) {
        // Navigate to performance detail view
        val action = PerformanceFragmentDirections.actionNavigationPerformanceToPerformanceDetailFragment(performance.id)
        findNavController().navigate(action)
    }
    
    override fun onDownloadClick(performance: Performance) {
        if (performance.isDownloaded) {
            // If already downloaded, navigate to performance detail view
            val action = PerformanceFragmentDirections.actionNavigationPerformanceToPerformanceDetailFragment(performance.id)
            findNavController().navigate(action)
        } else {
            // Start download
            viewModel.downloadPerformance(performance.id)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
