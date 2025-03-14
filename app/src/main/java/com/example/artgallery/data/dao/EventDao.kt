package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.artgallery.data.entity.Event

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events")
    fun getAllEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): Event?

    @Query("SELECT * FROM events WHERE category = :category")
    fun getEventsByCategory(category: String): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE startDate >= :startTime AND startDate <= :endTime")
    fun getEventsInTimeRange(startTime: Long, endTime: Long): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE startDate >= :today ORDER BY startDate ASC")
    fun getUpcomingEvents(today: Long): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE isUserAttending = 1")
    fun getUserAttendingEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE hasReminderSet = 1")
    fun getEventsWithReminders(): LiveData<List<Event>>

    @Query("UPDATE events SET isUserAttending = :isAttending WHERE id = :eventId")
    suspend fun updateUserAttendance(eventId: Long, isAttending: Boolean)

    @Query("UPDATE events SET hasReminderSet = :hasReminder, reminderTime = :reminderTime WHERE id = :eventId")
    suspend fun updateEventReminder(eventId: Long, hasReminder: Boolean, reminderTime: Long?)

    @Query("UPDATE events SET currentAttendees = currentAttendees + 1 WHERE id = :eventId")
    suspend fun incrementAttendeeCount(eventId: Long)

    @Query("UPDATE events SET currentAttendees = currentAttendees - 1 WHERE id = :eventId AND currentAttendees > 0")
    suspend fun decrementAttendeeCount(eventId: Long)
}
