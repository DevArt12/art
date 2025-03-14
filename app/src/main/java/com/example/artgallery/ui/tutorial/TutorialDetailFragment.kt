package com.example.artgallery.ui.tutorial

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.artgallery.R
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.databinding.FragmentTutorialDetailBinding
import com.example.artgallery.viewmodel.TutorialViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for viewing and playing tutorial videos
 */
class TutorialDetailFragment : Fragment() {

    private var _binding: FragmentTutorialDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TutorialViewModel by viewModels()
    private val args: TutorialDetailFragmentArgs by navArgs()
    
    private var mediaController: MediaController? = null
    private var currentTutorial: Tutorial? = null
    private var currentPosition = 0
    private var isPlaying = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMenuProvider()
        setupVideoView()
        
        // Load tutorial details
        loadTutorialDetails(args.tutorialId)
        
        // Set up progress tracking
        binding.buttonMarkProgress.setOnClickListener {
            showProgressDialog()
        }
        
        // Set up quiz button
        binding.buttonTakeQuiz.setOnClickListener {
            startQuiz()
        }
        
        // Set up download button
        binding.buttonDownload.setOnClickListener {
            currentTutorial?.let { tutorial ->
                if (tutorial.isDownloaded) {
                    showDownloadOptions(tutorial)
                } else {
                    viewModel.downloadTutorial(tutorial.id)
                }
            }
        }
        
        // Observe download progress
        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { status ->
            status?.let { 
                updateDownloadProgress(it.tutorialId, it.progress)
                
                if (it.progress == 100) {
                    Snackbar.make(binding.root, "Tutorial downloaded successfully", Snackbar.LENGTH_SHORT).show()
                    viewModel.clearDownloadStatus()
                }
            }
        })
    }
    
    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_tutorial_detail, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        shareTutorial()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun setupVideoView() {
        // Set up media controller
        mediaController = MediaController(requireContext())
        mediaController?.setAnchorView(binding.videoView)
        
        // Set up video view
        binding.videoView.setMediaController(mediaController)
        
        // Set up video completion listener
        binding.videoView.setOnCompletionListener {
            // When video completes, update progress to 100% if not already
            currentTutorial?.let { tutorial ->
                if (tutorial.userProgress < 100) {
                    viewModel.updateUserProgress(tutorial.id, 100)
                    Snackbar.make(binding.root, "Tutorial completed! Take the quiz to test your knowledge.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        
        // Set up error listener
        binding.videoView.setOnErrorListener { _, what, extra ->
            Snackbar.make(binding.root, "Error playing video: $what, $extra", Snackbar.LENGTH_LONG).show()
            true
        }
        
        // Save video position when user seeks
        binding.videoView.setOnPreparedListener { mp ->
            mp.setOnSeekCompleteListener {
                currentPosition = binding.videoView.currentPosition
            }
        }
    }
    
    private fun loadTutorialDetails(tutorialId: Long) {
        viewModel.getTutorialById(tutorialId).observe(viewLifecycleOwner, Observer { tutorial ->
            tutorial?.let {
                currentTutorial = it
                displayTutorialDetails(it)
                prepareVideo(it)
                updateProgressUI(it)
                updateDownloadButton(it)
            } ?: run {
                // Tutorial not found
                Snackbar.make(binding.root, "Tutorial not found", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        })
    }
    
    private fun displayTutorialDetails(tutorial: Tutorial) {
        binding.textTitle.text = tutorial.title
        binding.textDescription.text = tutorial.description
        binding.textCategory.text = tutorial.category.capitalize()
        binding.textDifficulty.text = tutorial.difficulty.capitalize()
        
        // Format and display duration
        binding.textDuration.text = viewModel.formatDuration(tutorial.duration)
        
        // Set difficulty chip color based on level
        val difficultyColorRes = when (tutorial.difficulty) {
            Tutorial.DIFFICULTY_BEGINNER -> R.color.colorBeginner
            Tutorial.DIFFICULTY_INTERMEDIATE -> R.color.colorIntermediate
            Tutorial.DIFFICULTY_ADVANCED -> R.color.colorAdvanced
            else -> R.color.colorPrimary
        }
        binding.chipDifficulty.setChipBackgroundColorResource(difficultyColorRes)
        
        // Format and display date added
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        binding.textDateAdded.text = "Added ${dateFormat.format(Date(tutorial.dateAdded))}"
        
        // Show materials needed if available
        if (tutorial.materialsNeeded.isNullOrBlank()) {
            binding.cardMaterials.visibility = View.GONE
        } else {
            binding.cardMaterials.visibility = View.VISIBLE
            binding.textMaterialsList.text = tutorial.materialsNeeded
        }
    }
    
    private fun prepareVideo(tutorial: Tutorial) {
        val videoFile = File(tutorial.videoPath)
        
        if (videoFile.exists()) {
            // Play from local file
            binding.videoView.setVideoURI(Uri.fromFile(videoFile))
            binding.textVideoStatus.visibility = View.GONE
        } else {
            // Try to play from assets or resource
            try {
                val assetPath = "android.resource://${requireContext().packageName}/raw/${tutorial.videoPath}"
                binding.videoView.setVideoURI(Uri.parse(assetPath))
                binding.textVideoStatus.visibility = View.GONE
            } catch (e: Exception) {
                // Show download prompt
                binding.textVideoStatus.visibility = View.VISIBLE
                binding.textVideoStatus.text = getString(R.string.download_to_watch)
                e.printStackTrace()
            }
        }
    }
    
    private fun updateProgressUI(tutorial: Tutorial) {
        // Update progress bar
        binding.progressBar.progress = tutorial.userProgress
        binding.textProgress.text = "${tutorial.userProgress}% completed"
        
        // Show/hide quiz button based on progress
        binding.buttonTakeQuiz.isEnabled = tutorial.userProgress >= 75
        
        // Show completed badge if quiz is completed
        binding.imageBadgeCompleted.visibility = if (tutorial.hasCompletedQuiz) View.VISIBLE else View.GONE
        
        // Update progress button text based on progress
        if (tutorial.userProgress >= 100) {
            binding.buttonMarkProgress.text = getString(R.string.completed)
            binding.buttonMarkProgress.isEnabled = false
        } else {
            binding.buttonMarkProgress.text = getString(R.string.update_progress)
            binding.buttonMarkProgress.isEnabled = true
        }
    }
    
    private fun updateDownloadButton(tutorial: Tutorial) {
        if (tutorial.isDownloaded) {
            binding.buttonDownload.setIconResource(R.drawable.ic_downloaded)
            binding.buttonDownload.setText(R.string.downloaded)
            binding.downloadProgressBar.visibility = View.GONE
        } else {
            binding.buttonDownload.setIconResource(R.drawable.ic_download)
            binding.buttonDownload.setText(R.string.download)
            binding.downloadProgressBar.visibility = View.GONE
        }
    }
    
    private fun updateDownloadProgress(tutorialId: Long, progress: Int) {
        currentTutorial?.let { tutorial ->
            if (tutorial.id == tutorialId) {
                if (progress < 100) {
                    binding.buttonDownload.text = "$progress%"
                    binding.buttonDownload.isEnabled = false
                    binding.downloadProgressBar.visibility = View.VISIBLE
                    binding.downloadProgressBar.progress = progress
                } else {
                    updateDownloadButton(tutorial.copy(isDownloaded = true))
                }
            }
        }
    }
    
    private fun showProgressDialog() {
        val progress = currentTutorial?.userProgress ?: 0
        val progressOptions = arrayOf("25%", "50%", "75%", "100%")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.update_progress)
            .setSingleChoiceItems(progressOptions, progress / 25 - 1) { dialog, which ->
                val newProgress = (which + 1) * 25
                currentTutorial?.let { tutorial ->
                    viewModel.updateUserProgress(tutorial.id, newProgress)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun startQuiz() {
        val action = TutorialDetailFragmentDirections.actionTutorialDetailToQuizFragment(args.tutorialId)
        findNavController().navigate(action)
    }
    
    private fun showDownloadOptions(tutorial: Tutorial) {
        val options = arrayOf("Delete Download", "Cancel")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Download Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> viewModel.deleteTutorialDownload(tutorial.id)
                    1 -> dialog.dismiss()
                }
            }
            .show()
    }
    
    private fun shareTutorial() {
        currentTutorial?.let { tutorial ->
            // In a real app, we would create a shareable link
            Snackbar.make(binding.root, "Sharing functionality will be implemented soon", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Save current position and playing state
        if (binding.videoView.isPlaying) {
            currentPosition = binding.videoView.currentPosition
            isPlaying = true
            binding.videoView.pause()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Restore playing state
        if (isPlaying) {
            binding.videoView.seekTo(currentPosition)
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
}
