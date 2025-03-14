package com.example.artgallery.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentImageDetailBinding
import com.example.artgallery.utils.CustomGestureDetector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.chrisbanes.photoview.PhotoView

class ImageDetailFragment : Fragment(), CustomGestureDetector.GestureListener {
    
    private var _binding: FragmentImageDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var gestureDetector: CustomGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up shared element transition
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.shared_image)
        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupGestureDetector()
        setupBottomSheet()
        setupImageView()
        setupToolbar()
    }

    private fun setupGestureDetector() {
        gestureDetector = CustomGestureDetector(requireContext(), this)
        gestureDetector.attachToView(binding.photoView)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        
        // Add bottom sheet callback for custom animations
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Handle state changes
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Animate toolbar alpha based on slide
                binding.toolbar.alpha = 1 - slideOffset
            }
        })
    }

    private fun setupImageView() {
        ViewCompat.setTransitionName(binding.photoView, "artwork_image")
        
        // Load image with transition
        arguments?.getString("imageUrl")?.let { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.photoView)
                .also {
                    startPostponedEnterTransition()
                }
        }

        // Setup PhotoView
        binding.photoView.apply {
            maximumScale = 5f
            mediumScale = 3f
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    // Gesture callbacks
    override fun onSwipeDown() {
        if (binding.photoView.scale == 1f) {
            requireActivity().onBackPressed()
        }
    }

    override fun onSwipeUp() {
        if (binding.photoView.scale == 1f) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDoubleTap() {
        if (binding.photoView.scale > 1f) {
            binding.photoView.setScale(1f, true)
        } else {
            binding.photoView.setScale(3f, true)
        }
    }

    override fun onSwipeRight() {
        // Handle swipe right
    }

    override fun onSwipeLeft() {
        // Handle swipe left
    }

    override fun onLongPress() {
        // Handle long press
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
