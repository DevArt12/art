package com.example.artgallery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentFilterBottomSheetBinding
import com.example.artgallery.model.ArtworkFilter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class FilterBottomSheetFragment(
    private val onFilterApplied: (ArtworkFilter) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val selectedCategories = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryChips()
        setupApplyButton()
    }

    private fun setupCategoryChips() {
        val categories = resources.getStringArray(R.array.artwork_categories)
        categories.forEach { category ->
            val chip = layoutInflater.inflate(
                R.layout.item_filter_chip,
                binding.chipGroupCategories,
                false
            ) as Chip
            
            chip.text = category
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupApplyButton() {
        binding.btnApplyFilter.setOnClickListener {
            val searchQuery = binding.etSearch.text.toString()
            val filter = ArtworkFilter(
                query = searchQuery,
                selectedCategories = selectedCategories.toList()
            )
            onFilterApplied(filter)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
