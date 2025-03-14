package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val imagePath: String? = null,
    val location: String,
    val startDate: Long,
    val endDate: Long,
    val category: String,
    val organizer: String,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val website: String? = null,
    val isFree: Boolean = false,
    val ticketPrice: Double? = null,
    val hasReminderSet: Boolean = false,
    val reminderTime: Long? = null,
    val isUserAttending: Boolean = false,
    val maxAttendees: Int? = null,
    val currentAttendees: Int = 0
) : Parcelable {
    companion object {
        const val CATEGORY_EXHIBITION = "exhibition"
        const val CATEGORY_WORKSHOP = "workshop"
        const val CATEGORY_OPENING = "opening"
        const val CATEGORY_AUCTION = "auction"
        const val CATEGORY_PERFORMANCE = "performance"
        const val CATEGORY_LECTURE = "lecture"
    }
}
