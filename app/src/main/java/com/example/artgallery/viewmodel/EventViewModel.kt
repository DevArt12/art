package com.example.artgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.Event
import com.example.artgallery.data.repository.EventRepository
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for handling event-related operations
 */
class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    
    // LiveData for all events
    private val allEvents: LiveData<List<Event>>
    
    // Filtered events based on category, date range, and attending status
    val filteredEvents = MediatorLiveData<List<Event>>()
    
    // Current event being viewed
    val currentEvent = MutableLiveData<Event>()
    
    // Filter states
    val currentCategoryFilter = MutableLiveData<String?>(null)
    val isAttendingFilterActive = MutableLiveData<Boolean>(false)
    val hasDateRangeFilter = MutableLiveData<Boolean>(false)
    private val dateRangeStart = MutableLiveData<Long>()
    private val dateRangeEnd = MutableLiveData<Long>()
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        
        // Initialize with all events
        allEvents = repository.getAllEvents()
        
        // Set up the filtered events
        filteredEvents.addSource(allEvents) { events ->
            applyFilters(events)
        }
        
        filteredEvents.addSource(currentCategoryFilter) { _ ->
            applyFilters(allEvents.value ?: emptyList())
        }
        
        filteredEvents.addSource(isAttendingFilterActive) { _ ->
            applyFilters(allEvents.value ?: emptyList())
        }
        
        filteredEvents.addSource(hasDateRangeFilter) { _ ->
            applyFilters(allEvents.value ?: emptyList())
        }
        
        filteredEvents.addSource(dateRangeStart) { _ ->
            if (hasDateRangeFilter.value == true) {
                applyFilters(allEvents.value ?: emptyList())
            }
        }
        
        filteredEvents.addSource(dateRangeEnd) { _ ->
            if (hasDateRangeFilter.value == true) {
                applyFilters(allEvents.value ?: emptyList())
            }
        }
    }
    
    /**
     * Apply all active filters to the events list
     */
    private fun applyFilters(events: List<Event>) {
        var filtered = events
        
        // Apply category filter
        currentCategoryFilter.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        // Apply attending filter
        if (isAttendingFilterActive.value == true) {
            filtered = filtered.filter { it.isUserAttending }
        }
        
        // Apply date range filter
        if (hasDateRangeFilter.value == true) {
            val start = dateRangeStart.value ?: 0
            val end = dateRangeEnd.value ?: Long.MAX_VALUE
            
            filtered = filtered.filter { event ->
                // Event overlaps with the selected date range
                (event.startDate <= end && event.endDate >= start)
            }
        }
        
        // Sort by start date (upcoming first)
        filtered = filtered.sortedBy { it.startDate }
        
        filteredEvents.value = filtered
    }
    
    /**
     * Set the category filter
     */
    fun setCategoryFilter(category: String?) {
        currentCategoryFilter.value = category
    }
    
    /**
     * Set the attending filter
     */
    fun setAttendingFilter(attending: Boolean) {
        isAttendingFilterActive.value = attending
    }
    
    /**
     * Set the date range filter
     */
    fun setDateRangeFilter(start: Long, end: Long) {
        dateRangeStart.value = start
        dateRangeEnd.value = end
        hasDateRangeFilter.value = true
    }
    
    /**
     * Clear the date range filter
     */
    fun clearDateRangeFilter() {
        hasDateRangeFilter.value = false
    }
    
    /**
     * Get an event by ID
     */
    fun getEventById(eventId: Long): LiveData<Event?> {
        val result = MutableLiveData<Event?>()
        
        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            result.postValue(event)
            currentEvent.postValue(event)
        }
        
        return result
    }
    
    /**
     * Update user attendance for an event
     */
    fun updateAttendance(eventId: Long, isAttending: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateUserAttendance(eventId, isAttending)
                
                // Update attendee count
                if (isAttending) {
                    repository.incrementAttendeeCount(eventId)
                } else {
                    repository.decrementAttendeeCount(eventId)
                }
                
                // Update current event if it's the one being viewed
                currentEvent.value?.let { event ->
                    if (event.id == eventId) {
                        val updatedEvent = event.copy(
                            isUserAttending = isAttending,
                            currentAttendees = if (isAttending) {
                                event.currentAttendees + 1
                            } else {
                                (event.currentAttendees - 1).coerceAtLeast(0)
                            }
                        )
                        currentEvent.postValue(updatedEvent)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to update attendance: ${e.message}")
            }
        }
    }
    
    /**
     * Set a reminder for an event
     */
    fun setReminder(eventId: Long, reminderTime: Long) {
        viewModelScope.launch {
            try {
                repository.updateEventReminder(eventId, true, reminderTime)
                
                // Update current event if it's the one being viewed
                currentEvent.value?.let { event ->
                    if (event.id == eventId) {
                        val updatedEvent = event.copy(
                            hasReminderSet = true,
                            reminderTime = reminderTime
                        )
                        currentEvent.postValue(updatedEvent)
                    }
                }
                
                // In a real app, we would also schedule a notification here
                scheduleReminderNotification(eventId, reminderTime)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to set reminder: ${e.message}")
            }
        }
    }
    
    /**
     * Remove a reminder for an event
     */
    fun removeReminder(eventId: Long) {
        viewModelScope.launch {
            try {
                repository.updateEventReminder(eventId, false, null)
                
                // Update current event if it's the one being viewed
                currentEvent.value?.let { event ->
                    if (event.id == eventId) {
                        val updatedEvent = event.copy(
                            hasReminderSet = false,
                            reminderTime = null
                        )
                        currentEvent.postValue(updatedEvent)
                    }
                }
                
                // In a real app, we would also cancel the notification here
                cancelReminderNotification(eventId)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to remove reminder: ${e.message}")
            }
        }
    }
    
    /**
     * Refresh events data
     */
    fun refreshEvents() {
        // In a real app with a remote data source, this would fetch fresh data
        // For this offline app, we'll just simulate a refresh
        viewModelScope.launch {
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // No actual refresh needed for local database
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to refresh events: ${e.message}")
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Schedule a reminder notification
     * This is a placeholder for actual notification scheduling
     */
    private fun scheduleReminderNotification(eventId: Long, reminderTime: Long) {
        // In a real app, this would use WorkManager or AlarmManager to schedule a notification
        // For this example, we'll just log the action
        android.util.Log.d("EventViewModel", "Scheduled reminder for event $eventId at time $reminderTime")
    }
    
    /**
     * Cancel a reminder notification
     * This is a placeholder for actual notification cancellation
     */
    private fun cancelReminderNotification(eventId: Long) {
        // In a real app, this would cancel the scheduled notification
        // For this example, we'll just log the action
        android.util.Log.d("EventViewModel", "Cancelled reminder for event $eventId")
    }
}
