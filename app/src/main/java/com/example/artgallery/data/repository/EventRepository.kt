package com.example.artgallery.data.repository

import androidx.lifecycle.LiveData
import com.example.artgallery.data.dao.EventDao
import com.example.artgallery.data.entity.Event

/**
 * Repository for handling Event data operations
 */
class EventRepository(private val eventDao: EventDao) {

    /**
     * Get all events
     */
    fun getAllEvents(): LiveData<List<Event>> {
        return eventDao.getAllEvents()
    }

    /**
     * Get events by category
     */
    fun getEventsByCategory(category: String): LiveData<List<Event>> {
        return eventDao.getEventsByCategory(category)
    }

    /**
     * Get events in a specific time range
     */
    fun getEventsInTimeRange(startTime: Long, endTime: Long): LiveData<List<Event>> {
        return eventDao.getEventsInTimeRange(startTime, endTime)
    }

    /**
     * Get upcoming events
     */
    fun getUpcomingEvents(today: Long): LiveData<List<Event>> {
        return eventDao.getUpcomingEvents(today)
    }

    /**
     * Get events the user is attending
     */
    fun getUserAttendingEvents(): LiveData<List<Event>> {
        return eventDao.getUserAttendingEvents()
    }

    /**
     * Get events with reminders set
     */
    fun getEventsWithReminders(): LiveData<List<Event>> {
        return eventDao.getEventsWithReminders()
    }

    /**
     * Get an event by ID
     */
    suspend fun getEventById(eventId: Long): Event? {
        return eventDao.getEventById(eventId)
    }

    /**
     * Insert a new event
     */
    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    /**
     * Update an existing event
     */
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    /**
     * Delete an event
     */
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    /**
     * Update user attendance for an event
     */
    suspend fun updateUserAttendance(eventId: Long, isAttending: Boolean) {
        eventDao.updateUserAttendance(eventId, isAttending)
    }

    /**
     * Update event reminder settings
     */
    suspend fun updateEventReminder(eventId: Long, hasReminder: Boolean, reminderTime: Long?) {
        eventDao.updateEventReminder(eventId, hasReminder, reminderTime)
    }

    /**
     * Increment attendee count for an event
     */
    suspend fun incrementAttendeeCount(eventId: Long) {
        eventDao.incrementAttendeeCount(eventId)
    }

    /**
     * Decrement attendee count for an event
     */
    suspend fun decrementAttendeeCount(eventId: Long) {
        eventDao.decrementAttendeeCount(eventId)
    }
}
