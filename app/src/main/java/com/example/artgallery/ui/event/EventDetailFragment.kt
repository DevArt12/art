package com.example.artgallery.ui.event

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentEventDetailBinding
import com.example.artgallery.viewmodel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for viewing detailed information about an event
 */
class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EventViewModel by viewModels()
    private val args: EventDetailFragmentArgs by navArgs()
    
    private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up back button
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        
        // Load event details
        loadEventDetails()
        
        // Set up action buttons
        setupActionButtons()
    }
    
    private fun loadEventDetails() {
        val eventId = args.eventId
        
        viewModel.getEventById(eventId).observe(viewLifecycleOwner, Observer { event ->
            if (event == null) {
                showError(getString(R.string.event_not_found))
                return@Observer
            }
            
            // Update UI with event info
            binding.textTitle.text = event.title
            binding.textDescription.text = event.description
            binding.textCategory.text = event.category.capitalize()
            binding.textLocation.text = event.location
            binding.textOrganizer.text = event.organizer
            
            // Format and display dates
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
                binding.textPrice.text = getString(R.string.free_admission)
            } else {
                event.ticketPrice?.let {
                    binding.textPrice.text = getString(R.string.ticket_price_format, it)
                } ?: run {
                    binding.textPrice.visibility = View.GONE
                }
            }
            
            // Set attendance info
            event.maxAttendees?.let {
                binding.textAttendees.text = getString(
                    R.string.attendees_count,
                    event.currentAttendees,
                    it
                )
                
                // Show if event is full
                if (event.currentAttendees >= it) {
                    binding.textEventFull.visibility = View.VISIBLE
                } else {
                    binding.textEventFull.visibility = View.GONE
                }
            } ?: run {
                binding.textAttendees.text = getString(
                    R.string.attendees_count_unlimited,
                    event.currentAttendees
                )
                binding.textEventFull.visibility = View.GONE
            }
            
            // Set contact info
            val hasContactInfo = !event.contactEmail.isNullOrEmpty() || 
                                !event.contactPhone.isNullOrEmpty() ||
                                !event.website.isNullOrEmpty()
            
            if (hasContactInfo) {
                binding.cardContactInfo.visibility = View.VISIBLE
                
                // Email
                if (!event.contactEmail.isNullOrEmpty()) {
                    binding.textEmail.text = event.contactEmail
                    binding.layoutEmail.visibility = View.VISIBLE
                } else {
                    binding.layoutEmail.visibility = View.GONE
                }
                
                // Phone
                if (!event.contactPhone.isNullOrEmpty()) {
                    binding.textPhone.text = event.contactPhone
                    binding.layoutPhone.visibility = View.VISIBLE
                } else {
                    binding.layoutPhone.visibility = View.GONE
                }
                
                // Website
                if (!event.website.isNullOrEmpty()) {
                    binding.textWebsite.text = event.website
                    binding.layoutWebsite.visibility = View.VISIBLE
                } else {
                    binding.layoutWebsite.visibility = View.GONE
                }
            } else {
                binding.cardContactInfo.visibility = View.GONE
            }
            
            // Load event image
            loadEventImage(event.imagePath)
            
            // Update action buttons state
            updateActionButtonsState(event)
        })
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
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .into(binding.imageEvent)
            } else {
                // Try to load as URL
                Glide.with(this)
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
    
    private fun setupActionButtons() {
        // Set up attend button click
        binding.buttonAttend.setOnClickListener {
            val eventId = args.eventId
            val isCurrentlyAttending = binding.buttonAttend.text == getString(R.string.unattend)
            
            viewModel.updateAttendance(eventId, !isCurrentlyAttending)
            
            val message = if (!isCurrentlyAttending) {
                getString(R.string.marked_as_attending)
            } else {
                getString(R.string.unmarked_as_attending)
            }
            
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
        
        // Set up reminder button click
        binding.buttonReminder.setOnClickListener {
            val eventId = args.eventId
            val hasReminder = viewModel.currentEvent.value?.hasReminderSet ?: false
            val reminderTime = viewModel.currentEvent.value?.reminderTime
            
            // Show reminder dialog
            val reminderDialog = ReminderDialogFragment.newInstance(eventId, hasReminder, reminderTime)
            reminderDialog.show(childFragmentManager, "REMINDER_DIALOG")
        }
        
        // Set up share button click
        binding.buttonShare.setOnClickListener {
            val event = viewModel.currentEvent.value ?: return@setOnClickListener
            
            val shareText = buildShareText(event)
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, event.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_event)))
        }
        
        // Set up contact actions
        binding.layoutEmail.setOnClickListener {
            val email = binding.textEmail.text.toString()
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                }
                startActivity(intent)
            }
        }
        
        binding.layoutPhone.setOnClickListener {
            val phone = binding.textPhone.text.toString()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }
        
        binding.layoutWebsite.setOnClickListener {
            val website = binding.textWebsite.text.toString()
            if (website.isNotEmpty()) {
                var websiteUrl = website
                if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                    websiteUrl = "https://$websiteUrl"
                }
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(websiteUrl)
                }
                startActivity(intent)
            }
        }
    }
    
    private fun updateActionButtonsState(event: com.example.artgallery.data.entity.Event) {
        // Update attend button state
        if (event.isUserAttending) {
            binding.buttonAttend.text = getString(R.string.unattend)
            binding.buttonAttend.setIconResource(R.drawable.ic_remove_circle)
        } else {
            binding.buttonAttend.text = getString(R.string.attend)
            binding.buttonAttend.setIconResource(R.drawable.ic_add_circle)
        }
        
        // Disable attend button if event is full
        event.maxAttendees?.let {
            if (event.currentAttendees >= it && !event.isUserAttending) {
                binding.buttonAttend.isEnabled = false
                binding.buttonAttend.text = getString(R.string.event_full)
            } else {
                binding.buttonAttend.isEnabled = true
            }
        }
        
        // Update reminder button state
        if (event.hasReminderSet) {
            binding.buttonReminder.text = getString(R.string.edit_reminder)
            binding.buttonReminder.setIconResource(R.drawable.ic_edit_alarm)
        } else {
            binding.buttonReminder.text = getString(R.string.set_reminder)
            binding.buttonReminder.setIconResource(R.drawable.ic_add_alarm)
        }
    }
    
    private fun buildShareText(event: com.example.artgallery.data.entity.Event): String {
        val startDate = Date(event.startDate)
        val endDate = Date(event.endDate)
        
        val dateText = if (isSameDay(startDate, endDate)) {
            dateFormat.format(startDate)
        } else {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
        
        val timeText = "${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
        
        val priceText = if (event.isFree) {
            getString(R.string.free_admission)
        } else {
            event.ticketPrice?.let {
                getString(R.string.ticket_price_format, it)
            } ?: ""
        }
        
        return """
            ${event.title}
            
            ${event.description}
            
            Date: $dateText
            Time: $timeText
            Location: ${event.location}
            Category: ${event.category.capitalize()}
            Organizer: ${event.organizer}
            ${if (priceText.isNotEmpty()) "Price: $priceText" else ""}
            
            ${if (!event.website.isNullOrEmpty()) "Website: ${event.website}" else ""}
            
            Shared from Art Gallery App
        """.trimIndent()
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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
