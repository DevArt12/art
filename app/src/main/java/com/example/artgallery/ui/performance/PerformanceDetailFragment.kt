package com.example.artgallery.ui.performance

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentPerformanceDetailBinding
import com.example.artgallery.viewmodel.PerformanceViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for viewing and playing a performance video
 */
class PerformanceDetailFragment : Fragment() {

    private var _binding: FragmentPerformanceDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PerformanceViewModel by viewModels()
    private val args: PerformanceDetailFragmentArgs by navArgs()
    
    private var mediaController: MediaController? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up back button
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        
        // Load performance details
        loadPerformanceDetails()
        
        // Set up video player
        setupVideoPlayer()
        
        // Set up UI
        setupUI()
    }
    
    private fun loadPerformanceDetails() {
        val performanceId = args.performanceId
        
        viewModel.getPerformanceById(performanceId).observe(viewLifecycleOwner, Observer { performance ->
            if (performance == null) {
                showError(getString(R.string.performance_not_found))
                return@Observer
            }
            
            // Update UI with performance info
            updateUI(performance)
            
            // Load video
            loadVideo(performance.videoPath)
            
            // Increment view count
            viewModel.incrementViewCount(performance.id)
        })
    }
    
    private fun setupVideoPlayer() {
        // Create media controller
        mediaController = MediaController(requireContext())
        mediaController?.setAnchorView(binding.videoView)
        
        // Set up video view
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            // Hide loading indicator when video is ready
            binding.loadingIndicator.visibility = View.GONE
            
            // Start playback
            binding.videoView.start()
            viewModel.setPlaying(true)
            
            // Update duration text
            val duration = mediaPlayer.duration / 1000 // Convert to seconds
            binding.textDuration.text = viewModel.formatDuration(duration)
        }
        
        binding.videoView.setOnCompletionListener {
            viewModel.setPlaying(false)
        }
        
        binding.videoView.setOnErrorListener { _, what, extra ->
            showError("Video playback error: $what, $extra")
            binding.loadingIndicator.visibility = View.GONE
            true
        }
    }
    
    private fun loadVideo(videoPath: String) {
        if (videoPath.isEmpty()) {
            showError(getString(R.string.video_not_available))
            binding.loadingIndicator.visibility = View.GONE
            return
        }
        
        try {
            // Show loading indicator
            binding.loadingIndicator.visibility = View.VISIBLE
            
            // Check if video file exists
            val videoFile = File(videoPath)
            val videoUri = if (videoFile.exists()) {
                // Local file
                Uri.fromFile(videoFile)
            } else {
                // URL or resource
                Uri.parse(videoPath)
            }
            
            // Set video URI
            binding.videoView.setVideoURI(videoUri)
            
        } catch (e: Exception) {
            showError("Error loading video: ${e.message}")
            binding.loadingIndicator.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onPause() {
        super.onPause()
        // Save current position
        if (binding.videoView.isPlaying) {
            viewModel.updatePlaybackPosition(binding.videoView.currentPosition)
            binding.videoView.pause()
            viewModel.setPlaying(false)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Restore playback if it was playing
        if (viewModel.isPlaying.value == true) {
            binding.videoView.seekTo(viewModel.currentPlaybackPosition.value ?: 0)
            binding.videoView.start()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.videoView.stopPlayback()
        _binding = null
    }
    
    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
    
    private fun setupUI() {
        // Set up toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set up video controls
        binding.buttonPlayVideo.setOnClickListener {
            startVideoPlayback()
        }
    }

    private fun updateUI(performance: Performance) {
        binding.textTitle.text = performance.title
        binding.textArtist.text = "By ${performance.artistName}"
        binding.textDate.text = formatDate(performance.dateRecorded)
        binding.chipCategory.text = performance.category
        binding.chipDuration.text = formatDuration(performance.duration)
        binding.textDescription.text = performance.description

        // Load thumbnail
        if (performance.thumbnailPath.isNotEmpty()) {
            Glide.with(this)
                .load(performance.thumbnailPath)
                .placeholder(R.drawable.placeholder_performance)
                .error(R.drawable.placeholder_performance)
                .into(binding.imagePerformance)
        }

        // Update download/play button
        if (performance.isDownloaded) {
            binding.fabDownload.setImageResource(R.drawable.ic_play)
            binding.fabDownload.setOnClickListener {
                startVideoPlayback()
            }
        } else {
            binding.fabDownload.setImageResource(R.drawable.ic_download)
            binding.fabDownload.setOnClickListener {
                startDownload()
            }
        }
    }

    private fun startVideoPlayback() {
        binding.progressVideo.visibility = View.VISIBLE
        binding.buttonPlayVideo.visibility = View.GONE
        binding.imagePerformance.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE

        // Set up video view
        binding.videoView.setVideoPath(currentPerformance?.videoPath)
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            binding.progressVideo.visibility = View.GONE
            mediaPlayer.start()
        }
        binding.videoView.setOnCompletionListener {
            binding.videoView.visibility = View.GONE
            binding.imagePerformance.visibility = View.VISIBLE
            binding.buttonPlayVideo.visibility = View.VISIBLE
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            binding.progressVideo.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            binding.imagePerformance.visibility = View.VISIBLE
            binding.buttonPlayVideo.visibility = View.VISIBLE
            binding.textVideoUnavailable.visibility = View.VISIBLE
            true
        }
    }
}
