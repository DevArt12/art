package com.example.artgallery.ui.event

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.artgallery.R
import com.example.artgallery.databinding.DialogReminderBinding
import com.example.artgallery.viewmodel.EventViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Dialog fragment for setting event reminders
 */
class ReminderDialogFragment : DialogFragment() {

    private var _binding: DialogReminderBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: EventViewModel by viewModels()
    
    private var eventId: Long = 0
    private var hasReminder: Boolean = false
    private var reminderTime: Long? = null
    
    companion object {
        private const val ARG_EVENT_ID = "event_id"
        private const val ARG_HAS_REMINDER = "has_reminder"
        private const val ARG_REMINDER_TIME = "reminder_time"
        
        fun newInstance(eventId: Long, hasReminder: Boolean, reminderTime: Long?): ReminderDialogFragment {
            return ReminderDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EVENT_ID, eventId)
                    putBoolean(ARG_HAS_REMINDER, hasReminder)
                    reminderTime?.let { putLong(ARG_REMINDER_TIME, it) }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        arguments?.let {
            eventId = it.getLong(ARG_EVENT_ID)
            hasReminder = it.getBoolean(ARG_HAS_REMINDER)
            if (it.containsKey(ARG_REMINDER_TIME)) {
                reminderTime = it.getLong(ARG_REMINDER_TIME)
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogReminderBinding.inflate(LayoutInflater.from(context))
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (hasReminder) R.string.edit_reminder else R.string.set_reminder)
            .setView(binding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                saveReminder()
            }
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(if (hasReminder) R.string.remove_reminder else null) { _, _ ->
                if (hasReminder) {
                    viewModel.removeReminder(eventId)
                }
            }
            .create()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupReminderOptions()
    }
    
    private fun setupReminderOptions() {
        // Set up reminder time options
        val reminderOptions = arrayOf(
            TimeUnit.MINUTES.toMillis(15),    // 15 minutes before
            TimeUnit.MINUTES.toMillis(30),    // 30 minutes before
            TimeUnit.HOURS.toMillis(1),       // 1 hour before
            TimeUnit.HOURS.toMillis(3),       // 3 hours before
            TimeUnit.HOURS.toMillis(6),       // 6 hours before
            TimeUnit.HOURS.toMillis(12),      // 12 hours before
            TimeUnit.DAYS.toMillis(1),        // 1 day before
            TimeUnit.DAYS.toMillis(2),        // 2 days before
            TimeUnit.DAYS.toMillis(7)         // 1 week before
        )
        
        // Set default selection based on current reminder time
        var selectedOption = 2 // Default to 1 hour before
        
        if (hasReminder && reminderTime != null) {
            // Find the closest option to the current reminder time
            val currentReminderOffset = reminderTime!!
            
            var minDifference = Long.MAX_VALUE
            reminderOptions.forEachIndexed { index, option ->
                val difference = Math.abs(option - currentReminderOffset)
                if (difference < minDifference) {
                    minDifference = difference
                    selectedOption = index
                }
            }
        }
        
        // Set up radio buttons
        val radioButtons = arrayOf(
            binding.radio15min,
            binding.radio30min,
            binding.radio1hour,
            binding.radio3hours,
            binding.radio6hours,
            binding.radio12hours,
            binding.radio1day,
            binding.radio2days,
            binding.radio1week
        )
        
        // Check the selected option
        radioButtons[selectedOption].isChecked = true
        
        // Set up custom date/time picker
        binding.switchCustomTime.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutCustomTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.radioGroup.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        
        // Set up date picker
        binding.buttonSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            // If we have a reminder time, use it to set the initial date
            if (hasReminder && reminderTime != null) {
                calendar.timeInMillis = reminderTime!!
            }
            
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    // Update button text
                    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    binding.buttonSelectDate.text = dateFormat.format(calendar.time)
                    
                    // Store the selected date
                    binding.buttonSelectDate.tag = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            datePickerDialog.show()
        }
        
        // Set up time picker
        binding.buttonSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            // If we have a reminder time, use it to set the initial time
            if (hasReminder && reminderTime != null) {
                calendar.timeInMillis = reminderTime!!
            }
            
            val timePickerDialog = android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    
                    // Update button text
                    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                    binding.buttonSelectTime.text = timeFormat.format(calendar.time)
                    
                    // Store the selected time
                    binding.buttonSelectTime.tag = calendar.timeInMillis
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            
            timePickerDialog.show()
        }
        
        // Initialize custom date/time if we have a reminder
        if (hasReminder && reminderTime != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = reminderTime!!
            
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            
            binding.buttonSelectDate.text = dateFormat.format(calendar.time)
            binding.buttonSelectDate.tag = calendar.timeInMillis
            
            binding.buttonSelectTime.text = timeFormat.format(calendar.time)
            binding.buttonSelectTime.tag = calendar.timeInMillis
        }
    }
    
    private fun saveReminder() {
        if (binding.switchCustomTime.isChecked) {
            // Custom date/time
            val dateMillis = binding.buttonSelectDate.tag as? Long
            val timeMillis = binding.buttonSelectTime.tag as? Long
            
            if (dateMillis == null || timeMillis == null) {
                return
            }
            
            // Combine date and time
            val calendar = Calendar.getInstance()
            
            // Set date components
            val dateCalendar = Calendar.getInstance()
            dateCalendar.timeInMillis = dateMillis
            calendar.set(
                dateCalendar.get(Calendar.YEAR),
                dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)
            )
            
            // Set time components
            val timeCalendar = Calendar.getInstance()
            timeCalendar.timeInMillis = timeMillis
            calendar.set(
                Calendar.HOUR_OF_DAY,
                timeCalendar.get(Calendar.HOUR_OF_DAY)
            )
            calendar.set(
                Calendar.MINUTE,
                timeCalendar.get(Calendar.MINUTE)
            )
            calendar.set(Calendar.SECOND, 0)
            
            // Save the reminder
            viewModel.setReminder(eventId, calendar.timeInMillis)
        } else {
            // Predefined time offset
            val selectedOffset = when {
                binding.radio15min.isChecked -> TimeUnit.MINUTES.toMillis(15)
                binding.radio30min.isChecked -> TimeUnit.MINUTES.toMillis(30)
                binding.radio1hour.isChecked -> TimeUnit.HOURS.toMillis(1)
                binding.radio3hours.isChecked -> TimeUnit.HOURS.toMillis(3)
                binding.radio6hours.isChecked -> TimeUnit.HOURS.toMillis(6)
                binding.radio12hours.isChecked -> TimeUnit.HOURS.toMillis(12)
                binding.radio1day.isChecked -> TimeUnit.DAYS.toMillis(1)
                binding.radio2days.isChecked -> TimeUnit.DAYS.toMillis(2)
                binding.radio1week.isChecked -> TimeUnit.DAYS.toMillis(7)
                else -> TimeUnit.HOURS.toMillis(1) // Default to 1 hour
            }
            
            // Get the event start time and subtract the offset
            viewModel.getEventById(eventId).observe(viewLifecycleOwner) { event ->
                if (event != null) {
                    val reminderTime = event.startDate - selectedOffset
                    viewModel.setReminder(eventId, reminderTime)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
