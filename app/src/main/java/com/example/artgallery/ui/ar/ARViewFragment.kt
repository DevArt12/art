package com.example.artgallery.ui.ar

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentArViewBinding
import com.example.artgallery.viewmodel.ARStudioViewModel
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import kotlinx.coroutines.launch
import java.io.File

/**
 * Extension function to convert File to Uri
 */
fun File.toUri(): Uri {
    return Uri.parse("file://${this.absolutePath}")
}

/**
 * Fragment for viewing AR models in augmented reality
 */
class ARViewFragment : Fragment() {

    private var _binding: FragmentArViewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ARStudioViewModel by viewModels()
    private val args: ARViewFragmentArgs by navArgs()
    
    private var modelNode: ArModelNode? = null
    private lateinit var sceneView: ArSceneView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize AR Scene View
        sceneView = binding.sceneView
        
        // Set up AR scene
        setupARScene()
        
        // Load the selected model
        loadSelectedModel()
        
        // Set up UI elements
        setupUI()
        
        // Observe tracking status
        observeTrackingStatus()
    }
    
    private fun setupARScene() {
        sceneView.apply {
            // Set up plane detection
            planeRenderer.isEnabled = true
            
            // Set up instructions
            instructions.enabled = true
            
            // Set up camera controls
            cameraController.enabled = true
        }
    }
    
    private fun loadSelectedModel() {
        // Get the model ID from navigation arguments
        val modelId = args.modelId
        
        // Show loading indicator
        binding.loadingIndicator.visibility = View.VISIBLE
        
        // Load the model from the ViewModel
        viewModel.getModelById(modelId).observe(viewLifecycleOwner, Observer { model ->
            if (model == null) {
                showError(getString(R.string.model_not_found))
                return@Observer
            }
            
            // Check if model is downloaded
            if (!model.isDownloaded) {
                showError(getString(R.string.model_not_downloaded))
                return@Observer
            }
            
            // Load the 3D model
            val modelFile = File(model.modelFilePath)
            if (!modelFile.exists()) {
                showError(getString(R.string.model_file_not_found))
                return@Observer
            }
            
            // Update UI with model info
            binding.textModelName.text = model.name
            binding.textModelDescription.text = model.description
            
            // Create model node
            lifecycleScope.launch {
                try {
                    modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
                        loadModelGlbAsync(
                            glbFileLocation = modelFile.toUri(),
                            autoAnimate = true,
                            scaleToUnits = 1.0f,
                            centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
                        )
                        onAnchorChanged = { anchor ->
                            viewModel.setModelPlaced(anchor != null)
                        }
                    }
                    
                    sceneView.addChild(modelNode!!)
                    
                    // Hide loading indicator
                    binding.loadingIndicator.visibility = View.GONE
                    
                    // Show placement instructions
                    binding.textPlacementInstructions.visibility = View.VISIBLE
                    
                } catch (e: Exception) {
                    showError("Error loading model: ${e.message}")
                }
            }
        })
    }
    
    private fun setupUI() {
        // Set up back button
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        
        // Set up reset button
        binding.buttonReset.setOnClickListener {
            // Reset placement
            modelNode?.anchor = null
            
            // Show placement instructions again
            binding.textPlacementInstructions.visibility = View.VISIBLE
        }
    }
    
    private fun observeTrackingStatus() {
        viewModel.trackingStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                ARStudioViewModel.TrackingStatus.TRACKING_NORMAL -> {
                    binding.textTrackingStatus.visibility = View.GONE
                }
                ARStudioViewModel.TrackingStatus.TRACKING_LIMITED -> {
                    binding.textTrackingStatus.visibility = View.VISIBLE
                    binding.textTrackingStatus.text = getString(R.string.tracking_limited)
                }
                ARStudioViewModel.TrackingStatus.NOT_TRACKING -> {
                    binding.textTrackingStatus.visibility = View.VISIBLE
                    binding.textTrackingStatus.text = getString(R.string.tracking_lost)
                }
            }
        })
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        binding.loadingIndicator.visibility = View.GONE
        binding.textPlacementInstructions.visibility = View.GONE
        binding.textTrackingStatus.visibility = View.VISIBLE
        binding.textTrackingStatus.text = message
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
