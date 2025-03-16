package com.example.artgallery.ui.forum

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
import com.example.artgallery.adapter.ForumPostAdapter
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.databinding.FragmentForumBinding
import com.example.artgallery.viewmodel.ForumViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for browsing and interacting with community forum posts
 */
class ForumFragment : Fragment(), ForumPostAdapter.ForumPostClickListener {

    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ForumViewModel by viewModels()
    private lateinit var adapter: ForumPostAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilters()
        observeViewModel()
        setupFab()
        
        // Set up swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPosts()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ForumPostAdapter(this)
        binding.recyclerViewPosts.adapter = adapter
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupCategoryFilters() {
        // Set up chip click listeners
        val chipClickListener = View.OnClickListener { view ->
            val chip = view as Chip
            
            // Set the category filter in the ViewModel
            val category = when (chip.id) {
                R.id.chip_all -> null // null means no filter (all posts)
                R.id.chip_general -> ForumPost.CATEGORY_GENERAL
                R.id.chip_techniques -> ForumPost.CATEGORY_TECHNIQUES
                R.id.chip_critique -> ForumPost.CATEGORY_CRITIQUE
                R.id.chip_events -> ForumPost.CATEGORY_EVENTS
                R.id.chip_marketplace -> ForumPost.CATEGORY_MARKETPLACE
                R.id.chip_collaboration -> ForumPost.CATEGORY_COLLABORATION
                else -> null
            }
            
            viewModel.setPostCategoryFilter(category)
        }
        
        // Apply the click listener to all chips
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_general).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_techniques).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_critique).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_events).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_marketplace).setOnClickListener(chipClickListener)
        binding.chipGroup.findViewById<Chip>(R.id.chip_collaboration).setOnClickListener(chipClickListener)
        
        // Select "All" by default
        binding.chipGroup.findViewById<Chip>(R.id.chip_all).isChecked = true
    }
    
    private fun setupFab() {
        binding.fabNewPost.setOnClickListener {
            val action = ForumFragmentDirections.actionNavigationForumToNewPostFragment()
            findNavController().navigate(action)
        }
    }
    
    private fun observeViewModel() {
        // Observe filtered posts
        viewModel.filteredPosts.observe(viewLifecycleOwner, Observer { posts ->
            adapter.submitList(posts)
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Show/hide empty state
            if (posts.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewPosts.visibility = View.GONE
                
                // Update empty state message based on current filter
                viewModel.currentCategoryFilter.value?.let { category ->
                    when (category) {
                        null -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_posts_available)
                            binding.textEmptyStateMessage.text = getString(R.string.be_first_to_post)
                        }
                        else -> {
                            binding.textEmptyStateTitle.text = getString(R.string.no_posts_in_category, category.capitalize())
                            binding.textEmptyStateMessage.text = getString(R.string.be_first_to_post_category, category.capitalize())
                        }
                    }
                }
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewPosts.visibility = View.VISIBLE
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
    
    override fun onPostClick(post: ForumPost) {
        // Navigate to post detail view
        val action = ForumFragmentDirections.actionNavigationForumToPostDetailFragment(post.id)
        findNavController().navigate(action)
        
        // Increment view count
        viewModel.incrementViewCount(post.id)
    }
    
    override fun onLikeClick(post: ForumPost) {
        viewModel.toggleLike(post.id)
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
