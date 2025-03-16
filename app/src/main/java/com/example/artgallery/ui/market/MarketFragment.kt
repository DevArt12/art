
package com.example.artgallery.ui.market

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.artgallery.R
import com.example.artgallery.adapter.ArtworkAdapter
import com.example.artgallery.databinding.FragmentMarketBinding
import com.example.artgallery.model.ArtworkFilter
import com.example.artgallery.ui.dialog.ArtworkDetailsDialogFragment
import com.example.artgallery.ui.dialog.ArtworkEditDialogFragment
import com.example.artgallery.viewmodel.ArtworkViewModel
import com.google.android.material.chip.Chip

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
        setupFilterChips()
        setupSortingSpinner()
        setupSearchView()
        observeArtworks()
        setupFab()
    }

    private fun setupRecyclerView() {
        artworkAdapter = ArtworkAdapter(
            onItemClick = { artwork ->
                ArtworkDetailsDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "artwork_details")
            },
            onEditClick = { artwork ->
                ArtworkEditDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "edit_artwork")
            },
            onDeleteClick = { artwork ->
                viewModel.deleteArtwork(artwork)
            }
        )
        
        binding.rvArtworks.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = artworkAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!viewModel.isLoading.value!! && !viewModel.isLastPage) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                            && firstVisibleItemPosition >= 0
                        ) {
                            viewModel.loadNextPage()
                        }
                    }
                }
            })
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshArtworks()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupSortingSpinner() {
        val sortOptions = arrayOf("Latest", "Price: Low to High", "Price: High to Low", "Most Popular")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerSort.adapter = adapter
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> viewModel.sortByDate()
                    1 -> viewModel.sortByPrice(ascending = true)
                    2 -> viewModel.sortByPrice(ascending = false)
                    3 -> viewModel.sortByPopularity()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFilterChips() {
        val categories = listOf("Paintings", "Sculptures", "Digital", "Photography", "Other")
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateFilter(category, isChecked)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupSortingSpinner() {
        binding.spinnerSort.setOnItemSelectedListener { position ->
            when (position) {
                0 -> viewModel.sortByPrice(ascending = true)
                1 -> viewModel.sortByPrice(ascending = false)
                2 -> viewModel.sortByDate(newest = true)
                3 -> viewModel.sortByPopularity()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchArtworks(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchArtworks(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddArtwork.setOnClickListener {
            ArtworkEditDialogFragment.newInstance(null)
                .show(childFragmentManager, "new_artwork")
        }
    }

    private fun observeArtworks() {
        viewModel.filteredArtworks.observe(viewLifecycleOwner) { artworks ->
            artworkAdapter.submitList(artworks)
            binding.tvNoResults.visibility = if (artworks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
