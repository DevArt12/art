package com.example.artgallery.ui.event

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
import com.example.artgallery.adapter.EventAdapter
import com.example.artgallery.data.entity.Event
import com.example.artgallery.databinding.FragmentEventBinding
import com.example.artgallery.viewmodel.EventViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragment for displaying and interacting with the event calendar
 */
class EventFragment : Fragment(), EventAdapter.EventClickListener {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EventViewModel by viewModels()
    private lateinit var adapter: EventAdapter
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCategoryFilters()
        setupDateFilter()
        setupAttendingFilter()
        observeViewModel()
        
        // Set up swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshEvents()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = EventAdapter(this)
        binding.recyclerViewEvents.adapter = adapter
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupCategoryFilters() {
        // Set up chip click listeners for categories
        binding.chipAll.setOnClickListener { viewModel.setCategoryFilter(null) }
        binding.chipExhibition.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_EXHIBITION) }
        binding.chipWorkshop.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_WORKSHOP) }
        binding.chipOpening.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_OPENING) }
        binding.chipAuction.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_AUCTION) }
        binding.chipPerformance.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_PERFORMANCE) }
        binding.chipLecture.setOnClickListener { viewModel.setCategoryFilter(Event.CATEGORY_LECTURE) }
    }
    
    private fun setupDateFilter() {
        // Set up date range picker
        binding.buttonDateFilter.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(androidx.core.util.Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds() + (30L * 24 * 60 * 60 * 1000) // Today + 30 days
                ))
                .build()
                
            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second
                
                // Update date filter button text
                binding.buttonDateFilter.text = getString(
                    R.string.date_range_filter,
                    dateFormat.format(Date(startDate)),
                    dateFormat.format(Date(endDate))
                )
                
                // Apply date filter
                viewModel.setDateRangeFilter(startDate, endDate)
            }
            
            dateRangePicker.addOnNegativeButtonClickListener {
                // Clear date filter
                binding.buttonDateFilter.text = getString(R.string.filter_by_date)
                viewModel.clearDateRangeFilter()
            }
            
            dateRangePicker.show(childFragmentManager, "DATE_PICKER")
        }
        
        // Set up quick date filters
        binding.chipToday.setOnClickListener {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis
            
            viewModel.setDateRangeFilter(startOfDay, endOfDay)
            binding.buttonDateFilter.text = getString(R.string.today)
        }
        
        binding.chipThisWeek.setOnClickListener {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            
            // Set to first day of week (Sunday or Monday depending on locale)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startOfWeek = calendar.timeInMillis
            
            // Add 6 days to get to end of week
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfWeek = calendar.timeInMillis
            
            viewModel.setDateRangeFilter(startOfWeek, endOfWeek)
            binding.buttonDateFilter.text = getString(R.string.this_week)
        }
        
        binding.chipThisMonth.setOnClickListener {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            
            // Set to first day of month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.timeInMillis
            
            // Set to last day of month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.timeInMillis
            
            viewModel.setDateRangeFilter(startOfMonth, endOfMonth)
            binding.buttonDateFilter.text = getString(R.string.this_month)
        }
    }
    
    private fun setupAttendingFilter() {
        binding.switchAttending.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAttendingFilter(isChecked)
        }
    }
    
    private fun observeViewModel() {
        // Observe filtered events
        viewModel.filteredEvents.observe(viewLifecycleOwner, Observer { events ->
            adapter.submitList(events)
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Show/hide empty state
            if (events.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewEvents.visibility = View.GONE
                
                // Update empty state message based on current filters
                updateEmptyStateMessage()
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewEvents.visibility = View.VISIBLE
            }
        })
        
        // Observe category filter changes
        viewModel.currentCategoryFilter.observe(viewLifecycleOwner, Observer { category ->
            updateCategoryChips(category)
        })
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        })
    }
    
    private fun updateCategoryChips(selectedCategory: String?) {
        binding.chipAll.isChecked = selectedCategory == null
        binding.chipExhibition.isChecked = selectedCategory == Event.CATEGORY_EXHIBITION
        binding.chipWorkshop.isChecked = selectedCategory == Event.CATEGORY_WORKSHOP
        binding.chipOpening.isChecked = selectedCategory == Event.CATEGORY_OPENING
        binding.chipAuction.isChecked = selectedCategory == Event.CATEGORY_AUCTION
        binding.chipPerformance.isChecked = selectedCategory == Event.CATEGORY_PERFORMANCE
        binding.chipLecture.isChecked = selectedCategory == Event.CATEGORY_LECTURE
    }
    
    private fun updateEmptyStateMessage() {
        val categoryFilter = viewModel.currentCategoryFilter.value
        val isAttendingFilter = viewModel.isAttendingFilterActive.value == true
        val hasDateFilter = viewModel.hasDateRangeFilter.value == true
        
        when {
            isAttendingFilter -> {
                binding.textEmptyStateTitle.text = getString(R.string.no_attending_events)
                binding.textEmptyStateMessage.text = getString(R.string.mark_events_to_attend)
            }
            hasDateFilter && categoryFilter != null -> {
                binding.textEmptyStateTitle.text = getString(
                    R.string.no_events_in_category_date_range,
                    categoryFilter.capitalize()
                )
                binding.textEmptyStateMessage.text = getString(R.string.try_different_filters)
            }
            hasDateFilter -> {
                binding.textEmptyStateTitle.text = getString(R.string.no_events_in_date_range)
                binding.textEmptyStateMessage.text = getString(R.string.try_different_date_range)
            }
            categoryFilter != null -> {
                binding.textEmptyStateTitle.text = getString(
                    R.string.no_events_in_category,
                    categoryFilter.capitalize()
                )
                binding.textEmptyStateMessage.text = getString(R.string.try_different_category)
            }
            else -> {
                binding.textEmptyStateTitle.text = getString(R.string.no_events_available)
                binding.textEmptyStateMessage.text = getString(R.string.check_back_later)
            }
        }
    }
    
    override fun onEventClick(event: Event) {
        // Navigate to event detail
        val action = EventFragmentDirections.actionNavigationEventToEventDetailFragment(event.id)
        findNavController().navigate(action)
    }
    
    override fun onAttendClick(event: Event, isAttending: Boolean) {
        viewModel.updateAttendance(event.id, isAttending)
        
        val message = if (isAttending) {
            getString(R.string.marked_attending, event.title)
        } else {
            getString(R.string.unmarked_attending, event.title)
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onReminderClick(event: Event) {
        // Show reminder dialog
        val reminderDialog = ReminderDialogFragment.newInstance(event.id, event.hasReminderSet, event.reminderTime)
        reminderDialog.show(childFragmentManager, "REMINDER_DIALOG")
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
