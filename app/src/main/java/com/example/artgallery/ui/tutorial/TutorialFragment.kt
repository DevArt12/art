package com.example.artgallery.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artgallery.R
import com.example.artgallery.adapter.TutorialAdapter
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.databinding.FragmentTutorialBinding
import com.example.artgallery.viewmodel.TutorialViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for browsing and interacting with art tutorials and classes
 */
class TutorialFragment : Fragment(), TutorialAdapter.TutorialClickListener {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TutorialViewModel by viewModels()
    private lateinit var tutorialsAdapter: TutorialAdapter
    private lateinit var inProgressAdapter: TutorialAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupCategoryFilters()
        setupDifficultyFilters()
        setupSearchFunctionality()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        // Main tutorials adapter
        tutorialsAdapter = TutorialAdapter(this)
        binding.recyclerViewTutorials.adapter = tutorialsAdapter
        binding.recyclerViewTutorials.layoutManager = GridLayoutManager(requireContext(), 2)
        
        // In-progress tutorials adapter
        inProgressAdapter = TutorialAdapter(this)
        binding.recyclerViewInProgress.adapter = inProgressAdapter
        binding.recyclerViewInProgress.layoutManager = 
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
    
    private fun setupCategoryFilters() {
        // Set up chip click listeners for categories
        binding.chipAll.setOnClickListener { viewModel.setTutorialCategoryFilter(null) }
        binding.chipPainting.setOnClickListener { viewModel.setTutorialCategoryFilter(Tutorial.CATEGORY_PAINTING) }
        binding.chipDrawing.setOnClickListener { viewModel.setTutorialCategoryFilter(Tutorial.CATEGORY_DRAWING) }
        binding.chipSculpture.setOnClickListener { viewModel.setTutorialCategoryFilter(Tutorial.CATEGORY_SCULPTURE) }
        binding.chipDigital.setOnClickListener { viewModel.setTutorialCategoryFilter(Tutorial.CATEGORY_DIGITAL) }
        binding.chipMixedMedia.setOnClickListener { viewModel.setTutorialCategoryFilter(Tutorial.CATEGORY_MIXED_MEDIA) }
    }
    
    private fun setupDifficultyFilters() {
        // Set up chip click listeners for difficulty levels
        binding.chipAllDifficulty.setOnClickListener { viewModel.setTutorialDifficultyFilter(null) }
        binding.chipBeginner.setOnClickListener { viewModel.setTutorialDifficultyFilter(Tutorial.DIFFICULTY_BEGINNER) }
        binding.chipIntermediate.setOnClickListener { viewModel.setTutorialDifficultyFilter(Tutorial.DIFFICULTY_INTERMEDIATE) }
        binding.chipAdvanced.setOnClickListener { viewModel.setTutorialDifficultyFilter(Tutorial.DIFFICULTY_ADVANCED) }
    }
    
    private fun setupSearchFunctionality() {
        // Set up search functionality
        binding.editTextSearch.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                viewModel.clearSearch()
            }
        }
        
        binding.editTextSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                if (query.isNotBlank()) {
                    viewModel.searchTutorials(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }
    
    private fun observeViewModel() {
        // Observe filtered tutorials
        viewModel.filteredTutorials.observe(viewLifecycleOwner, Observer { tutorials ->
            tutorialsAdapter.submitList(tutorials)
            
            // Show/hide empty state
            if (tutorials.isEmpty()) {
                binding.textNoResults.visibility = View.VISIBLE
                binding.recyclerViewTutorials.visibility = View.GONE
            } else {
                binding.textNoResults.visibility = View.GONE
                binding.recyclerViewTutorials.visibility = View.VISIBLE
            }
            
            // Hide progress bar
            binding.progressBar.visibility = View.GONE
        })
        
        // Observe in-progress tutorials
        viewModel.getInProgressTutorials().observe(viewLifecycleOwner, Observer { inProgressTutorials ->
            if (inProgressTutorials.isNotEmpty()) {
                binding.textInProgress.visibility = View.VISIBLE
                binding.recyclerViewInProgress.visibility = View.VISIBLE
                inProgressAdapter.submitList(inProgressTutorials)
            } else {
                binding.textInProgress.visibility = View.GONE
                binding.recyclerViewInProgress.visibility = View.GONE
            }
        })
        
        // Observe download progress
        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { status ->
            status?.let { 
                tutorialsAdapter.updateDownloadProgress(it.tutorialId, it.progress)
                inProgressAdapter.updateDownloadProgress(it.tutorialId, it.progress)
                
                if (it.progress == 100) {
                    Snackbar.make(binding.root, "Tutorial downloaded successfully", Snackbar.LENGTH_SHORT).show()
                    viewModel.clearDownloadStatus()
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
        
        // Observe category filter changes
        viewModel.currentCategoryFilter.observe(viewLifecycleOwner, Observer { category ->
            updateCategoryChips(category)
        })
        
        // Observe difficulty filter changes
        viewModel.currentDifficultyFilter.observe(viewLifecycleOwner, Observer { difficulty ->
            updateDifficultyChips(difficulty)
        })
    }
    
    private fun updateCategoryChips(selectedCategory: String?) {
        binding.chipAll.isChecked = selectedCategory == null
        binding.chipPainting.isChecked = selectedCategory == Tutorial.CATEGORY_PAINTING
        binding.chipDrawing.isChecked = selectedCategory == Tutorial.CATEGORY_DRAWING
        binding.chipSculpture.isChecked = selectedCategory == Tutorial.CATEGORY_SCULPTURE
        binding.chipDigital.isChecked = selectedCategory == Tutorial.CATEGORY_DIGITAL
        binding.chipMixedMedia.isChecked = selectedCategory == Tutorial.CATEGORY_MIXED_MEDIA
    }
    
    private fun updateDifficultyChips(selectedDifficulty: String?) {
        binding.chipAllDifficulty.isChecked = selectedDifficulty == null
        binding.chipBeginner.isChecked = selectedDifficulty == Tutorial.DIFFICULTY_BEGINNER
        binding.chipIntermediate.isChecked = selectedDifficulty == Tutorial.DIFFICULTY_INTERMEDIATE
        binding.chipAdvanced.isChecked = selectedDifficulty == Tutorial.DIFFICULTY_ADVANCED
    }
    
    // TutorialClickListener implementation
    override fun onTutorialClick(tutorial: Tutorial) {
        // Navigate to tutorial detail
        val action = TutorialFragmentDirections.actionTutorialFragmentToTutorialDetailFragment(tutorial.id)
        findNavController().navigate(action)
        
        // Increment view count
        viewModel.incrementViewCount(tutorial.id)
    }
    
    override fun onDownloadClick(tutorial: Tutorial) {
        if (tutorial.isDownloaded) {
            // Show options for downloaded tutorial
            Snackbar.make(binding.root, "Tutorial already downloaded", Snackbar.LENGTH_SHORT)
                .setAction("Delete") {
                    viewModel.deleteTutorialDownload(tutorial.id)
                }
                .show()
        } else {
            // Start download
            viewModel.downloadTutorial(tutorial.id)
            Snackbar.make(binding.root, "Downloading tutorial...", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
