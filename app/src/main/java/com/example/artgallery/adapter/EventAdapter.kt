package com.example.artgallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.data.entity.Event
import com.example.artgallery.databinding.ItemEventBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying events in a RecyclerView
 */
class EventAdapter(private val listener: EventClickListener) : 
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    interface EventClickListener {
        fun onEventClick(event: Event)
        fun onAttendClick(event: Event, isAttending: Boolean)
        fun onReminderClick(event: Event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEventClick(getItem(position))
                }
            }

            binding.buttonAttend.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = getItem(position)
                    listener.onAttendClick(event, !event.isUserAttending)
                }
            }

            binding.buttonReminder.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReminderClick(getItem(position))
                }
            }
        }

        fun bind(event: Event) {
            binding.textTitle.text = event.title
            binding.textLocation.text = event.location
            binding.textCategory.text = event.category.capitalize()
            
            // Format and display date
            val startDate = Date(event.startDate)
            val endDate = Date(event.endDate)
            
            binding.textDate.text = if (isSameDay(startDate, endDate)) {
                // Single day event
                dateFormat.format(startDate)
            } else {
                // Multi-day event
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            }
            
            binding.textTime.text = "${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
            
            // Set price info
            if (event.isFree) {
                binding.textPrice.text = binding.root.context.getString(R.string.free_admission)
                binding.textPrice.visibility = View.VISIBLE
            } else if (event.ticketPrice != null) {
                binding.textPrice.text = binding.root.context.getString(
                    R.string.ticket_price_format, 
                    event.ticketPrice
                )
                binding.textPrice.visibility = View.VISIBLE
            } else {
                binding.textPrice.visibility = View.GONE
            }
            
            // Set attendance status
            if (event.isUserAttending) {
                binding.buttonAttend.text = binding.root.context.getString(R.string.attending)
                binding.buttonAttend.setIconResource(R.drawable.ic_check_circle)
                binding.buttonAttend.setBackgroundColor(
                    binding.root.context.getColor(R.color.attending_green)
                )
            } else {
                binding.buttonAttend.text = binding.root.context.getString(R.string.attend)
                binding.buttonAttend.setIconResource(R.drawable.ic_add_circle)
                binding.buttonAttend.setBackgroundColor(
                    binding.root.context.getColor(R.color.colorPrimary)
                )
            }
            
            // Disable attend button if event is full
            event.maxAttendees?.let {
                if (event.currentAttendees >= it && !event.isUserAttending) {
                    binding.buttonAttend.isEnabled = false
                    binding.buttonAttend.text = binding.root.context.getString(R.string.event_full)
                } else {
                    binding.buttonAttend.isEnabled = true
                }
            }
            
            // Set reminder icon
            if (event.hasReminderSet) {
                binding.buttonReminder.setIconResource(R.drawable.ic_alarm_on)
            } else {
                binding.buttonReminder.setIconResource(R.drawable.ic_alarm_add)
            }
            
            // Load event image
            loadEventImage(event.imagePath)
        }
        
        private fun loadEventImage(imagePath: String?) {
            if (imagePath.isNullOrEmpty()) {
                // Use placeholder image
                binding.imageEvent.setImageResource(R.drawable.placeholder_event)
                return
            }
            
            try {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    // Load local image
                    Glide.with(binding.root.context)
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_event)
                        .error(R.drawable.placeholder_event)
                        .into(binding.imageEvent)
                } else {
                    // Try to load as URL
                    Glide.with(binding.root.context)
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_event)
                        .error(R.drawable.placeholder_event)
                        .into(binding.imageEvent)
                }
            } catch (e: Exception) {
                // Use placeholder on error
                binding.imageEvent.setImageResource(R.drawable.placeholder_event)
            }
        }
        
        private fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
            val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
            
            return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                   cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
        }
        
        // Extension function to capitalize first letter of a string
        private fun String.capitalize(): String {
            return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
