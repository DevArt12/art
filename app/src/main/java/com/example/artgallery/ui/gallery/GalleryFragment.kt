package com.example.artgallery.ui.gallery

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.adapter.ArtworkAdapter
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.DialogAddArtworkBinding
import com.example.artgallery.databinding.FragmentGalleryBinding
import com.example.artgallery.ui.dialog.ArtworkDetailsDialogFragment
import com.example.artgallery.ui.dialog.ArtworkEditDialogFragment
import com.example.artgallery.ui.dialog.FilterBottomSheetFragment
import com.example.artgallery.viewmodel.ArtworkViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtworkViewModel by viewModels()
    private lateinit var artworkAdapter: ArtworkAdapter
    private var currentDialogBinding: DialogAddArtworkBinding? = null
    private var selectedImageUri: Uri? = null
    private var isLoading = false
    private var dialog: Dialog? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                selectedImageUri = it
                currentDialogBinding?.let { binding ->
                    binding.imageLoadingProgress.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(selectedImageUri)
                        .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                            override fun onLoadFailed(e: Exception?, model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?, isFirstResource: Boolean): Boolean {
                                binding.imageLoadingProgress.visibility = View.GONE
                                showError(getString(R.string.error_loading_image))
                                return false
                            }

                            override fun onResourceReady(resource: android.graphics.drawable.Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                                binding.imageLoadingProgress.visibility = View.GONE
                                return false
                            }
                        })
                        .into(binding.artworkImage)
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showImageSourceMenu(currentDialogBinding?.btnChooseImage ?: return@registerForActivityResult)
        } else {
            showError(getString(R.string.msg_permissions_required))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupFilterButton()
        setupSwipeRefresh()
        observeArtworks()
        observeLoadingState()
        observeErrors()
    }

    private fun setupRecyclerView() {
        artworkAdapter = ArtworkAdapter(
            onItemClick = { artwork ->
                ArtworkDetailsDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "artwork_details")
            },
            onEditClick = { artwork ->
                ArtworkEditDialogFragment.newInstance(artwork)
                    .show(childFragmentManager, "edit_artwork")
            },
            onDeleteClick = { artwork ->
                viewModel.deleteArtwork(artwork)
                Snackbar.make(binding.root, getString(R.string.msg_artwork_deleted), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.action_undo)) {
                        viewModel.insertArtwork(artwork)
                    }
                    .show()
            }
        )

        val layoutManager = GridLayoutManager(context, 2)
        // Configure layout manager to handle different view types
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (artworkAdapter.getItemViewType(position)) {
                    ArtworkAdapter.VIEW_TYPE_LOADING -> 2 // Full width for loading item
                    else -> 1 // Normal width for artwork items
                }
            }
        }
        
        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = artworkAdapter
            
            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        
                        if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                            loadMoreArtworks()
                        }
                    }
                }
            })
        }

        // Initialize with first page load
        if (viewModel.artworks.value.isNullOrEmpty()) {
            loadMoreArtworks()
        }
    }

    private fun loadMoreArtworks() {
        if (!isLoading) {
            isLoading = true
            artworkAdapter.addLoadingFooter()
            viewModel.loadNextPage()
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddArtworkDialog()
        }
    }

    private fun setupFilterButton() {
        binding.fabFilter.setOnClickListener {
            FilterBottomSheetFragment { filter ->
                viewModel.applyFilter(filter)
            }.show(childFragmentManager, "FilterBottomSheet")
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshArtworks()
        }
    }

    private fun observeArtworks() {
        viewModel.filteredArtworks.observe(viewLifecycleOwner) { artworks ->
            updateArtworkList(artworks)
        }
    }
    
    private fun updateArtworkList(artworks: List<Artwork>) {
        artworkAdapter.submitArtworkList(artworks)
        
        // Show empty view if no artworks and not loading
        val showEmptyView = artworks.isEmpty() && !isLoading
        binding.emptyView.isVisible = showEmptyView
        binding.recyclerView.isVisible = artworks.isNotEmpty() || isLoading
        
        // Show shimmer effect during initial loading
        if (isLoading && artworks.isEmpty()) {
            binding.shimmerLayout.visibility = View.VISIBLE
            binding.shimmerLayout.startShimmer()
        } else {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
        }
        
        // Setup empty view add button
        binding.btnAddFirst.setOnClickListener {
            showAddArtworkDialog()
        }
    }
    
    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            
            // Only stop refreshing if it was refreshing
            if (!loading && binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            
            // Handle loading indicator
            if (!loading) {
                artworkAdapter.removeLoadingFooter()
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            } else if (artworkAdapter.itemCount == 0) {
                // Only show shimmer for initial load
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.shimmerLayout.startShimmer()
            } else {
                // For pagination, we use the loading footer instead of shimmer
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }
            
            // Update UI based on loading state
            updateEmptyState()
        }
    }
    
    private fun updateEmptyState() {
        val artworks = viewModel.filteredArtworks.value ?: emptyList()
        val showEmptyView = artworks.isEmpty() && !isLoading
        binding.emptyView.isVisible = showEmptyView
        binding.recyclerView.isVisible = artworks.isNotEmpty() || isLoading
    }
    
    private fun observeErrors() {
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                // Show error message
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.action_retry)) {
                        // Retry loading data
                        if (artworkAdapter.itemCount == 0) {
                            // If no items, reload first page
                            viewModel.refreshArtworks()
                        } else {
                            // Otherwise try loading next page
                            loadMoreArtworks()
                        }
                    }
                    .show()
                
                // Stop loading indicators
                binding.swipeRefreshLayout.isRefreshing = false
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                
                // Remove loading footer if present
                if (isLoading) {
                    isLoading = false
                    artworkAdapter.removeLoadingFooter()
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.action_dismiss)) {
                // Dismiss action
            }
            .show()
    }

    private fun showImageSourceMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_image_source, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_camera -> {
                    launchCamera()
                    true
                }
                R.id.action_gallery -> {
                    launchGallery()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun launchCamera() {
        ImagePicker.with(this)
            .crop()
            .cameraOnly()
            .createIntentFromDialog { intent ->
                imagePickerLauncher.launch(intent)
            }
    }

    private fun launchGallery() {
        ImagePicker.with(this)
            .crop()
            .galleryOnly()
            .createIntentFromDialog { intent ->
                imagePickerLauncher.launch(intent)
            }
    }

    private fun showAddArtworkDialog() {
        dialog = Dialog(requireContext(), R.style.Theme_ArtGallery_Dialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val dialogBinding = DialogAddArtworkBinding.inflate(layoutInflater)
            currentDialogBinding = dialogBinding
            setContentView(dialogBinding.root)
            
            // Setup category spinner
            val categories = resources.getStringArray(R.array.artwork_categories)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
            dialogBinding.spinnerCategory.setAdapter(adapter)
            
            // Setup for sale switch
            dialogBinding.switchForSale.setOnCheckedChangeListener { _, isChecked ->
                dialogBinding.tilContact.isVisible = isChecked
            }
            
            // Setup image picker button
            dialogBinding.btnChooseImage.setOnClickListener {
                checkAndRequestPermissions()
            }
            
            // Setup save button
            dialogBinding.btnSave.setOnClickListener {
                if (validateInputs(dialogBinding)) {
                    saveArtwork(dialogBinding)
                }
            }
            
            // Setup cancel button
            dialogBinding.btnCancel.setOnClickListener {
                dismiss()
            }
            
            show()
        }
    }

    private fun validateInputs(binding: DialogAddArtworkBinding): Boolean {
        var isValid = true
        
        // Validate title
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.etTitle.error = getString(R.string.error_required_field)
            isValid = false
        }
        
        // Validate image
        if (selectedImageUri == null) {
            showError(getString(R.string.error_image_required))
            isValid = false
        }
        
        // Validate price if for sale
        if (binding.switchForSale.isChecked) {
            if (binding.etPrice.text.isNullOrBlank()) {
                binding.etPrice.error = getString(R.string.error_required_field)
                isValid = false
            }
            
            if (binding.etContact.text.isNullOrBlank()) {
                binding.etContact.error = getString(R.string.error_required_field)
                isValid = false
            }
        }
        
        return isValid
    }

    private fun saveArtwork(binding: DialogAddArtworkBinding) {
        // Show progress
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
        binding.btnCancel.isEnabled = false
        
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val category = binding.spinnerCategory.text.toString()
        val isForSale = binding.switchForSale.isChecked
        val price = if (isForSale && !binding.etPrice.text.isNullOrBlank()) {
            binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        } else 0.0
        val contactDetails = if (isForSale) binding.etContact.text.toString() else ""
        
        val artwork = Artwork(
            title = title,
            description = description,
            category = category,
            imagePath = selectedImageUri.toString(),
            isForSale = isForSale,
            price = price,
            contactDetails = contactDetails
        )
        
        try {
            viewModel.insertArtwork(artwork)
            dialog?.dismiss()
            Snackbar.make(binding.root, getString(R.string.msg_artwork_saved), Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            binding.btnSave.isEnabled = true
            binding.btnCancel.isEnabled = true
            showError(getString(R.string.error_saving_artwork))
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            showImageSourceMenu(currentDialogBinding?.btnChooseImage ?: return)
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        dialog = null
        currentDialogBinding = null
        _binding = null
    }
}
