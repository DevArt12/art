package com.example.artgallery.ui.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.artgallery.R
import com.example.artgallery.adapter.ARModelAdapter
import com.example.artgallery.data.entity.ARModel
import com.example.artgallery.databinding.FragmentArModelSelectionBinding
import com.example.artgallery.viewmodel.ARStudioViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment for selecting AR models to view in AR
 */
class ARModelSelectionFragment : Fragment(), ARModelAdapter.ARModelClickListener {

    private var _binding: FragmentArModelSelectionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ARStudioViewModel by viewModels()
    private lateinit var adapter: ARModelAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArModelSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilters()
        observeViewModel()
        
        // Set up swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshModels()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ARModelAdapter(this)
        binding.recyclerViewModels.adapter = adapter
        binding.recyclerViewModels.layoutManager = GridLayoutManager(requireContext(), 2)
    }
    
    private fun setupCategoryFilters() {
        // Set up chip click listeners
        val chipClickListener = View.OnClickListener { view ->
            val chip = view as Chip
            
            // Set the category filter in the ViewModel
            val category = when (chip.id) {
                R.id.chip_all -> null // null means no filter (all models)
                R.id.chip_downloaded -> "downloaded"
                R.id.chip_sculpture -> ARModel.CATEGORY_SCULPTURE
                R.id.chip_painting -> ARModel.CATEGORY_PAINTING
                R.id.chip_installation -> ARModel.CATEGORY_INSTALLATION
                R.id.chip_character -> ARModel.CATEGORY_CHARACTER
                R.id.chip_furniture -> ARModel.CATEGORY_FURNITURE
                R.id.chip_abstract -> ARModel.CATEGORY_ABSTRACT
                else -> null
            }
            
            viewModel.setModelCategoryFilter(category)
        }
        
        // Apply the click listener to all chips
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_downloaded).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_sculpture).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_painting).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_installation).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_character).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_furniture).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_abstract).setOnClickListener(chipClickListener)
        
        // Select "All" by default
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).isChecked = true
    }
    
    private fun observeViewModel() {
        // Observe filtered models
        viewModel.filteredModels.observe(viewLifecycleOwner, Observer { models ->
            adapter.submitList(models)
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Show/hide empty state
            if (models.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewModels.visibility = View.GONE
                
                // Update empty state message based on current filter
                viewModel.currentCategoryFilter.value?.let { category ->
                    when (category) {
                        "downloaded" -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_downloaded_models)
                            binding.textEmptyStateMessage.text = getString(R.string.download_models_message)
                        }
                        null -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_models_available)
                            binding.textEmptyStateMessage.text = getString(R.string.check_back_later)
                        }
                        else -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_models_in_category, category.capitalize())
                            binding.textEmptyStateMessage.text = getString(R.string.try_different_category)
                        }
                    }
                }
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewModels.visibility = View.VISIBLE
            }
        })
        
        // Observe download progress
        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is ARStudioViewModel.DownloadStatus.InProgress -> {
                    adapter.updateDownloadProgress(status.modelId, status.progress)
                }
                is ARStudioViewModel.DownloadStatus.Success -> {
                    adapter.updateDownloadProgress(status.modelId, 100)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.download_complete),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is ARStudioViewModel.DownloadStatus.Failed -> {
                    adapter.updateDownloadProgress(status.modelId, 0)
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
    
    override fun onViewModelClick(model: ARModel) {
        // Navigate to AR view if model is downloaded
        if (model.isDownloaded) {
            navigateToARView(model.id)
        } else {
            // Show message that model needs to be downloaded first
            Snackbar.make(
                binding.root,
                getString(R.string.download_model_first),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onDownloadClick(model: ARModel) {
        if (model.isDownloaded) {
            // If already downloaded, navigate to AR view
            navigateToARView(model.id)
        } else {
            // Start download
            viewModel.downloadModel(model.id)
        }
    }
    
    private fun navigateToARView(modelId: Long) {
        val action = ARModelSelectionFragmentDirections.actionNavigationArStudioToARViewFragment(modelId)
        findNavController().navigate(action)
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
