package com.example.artgallery.ui.forum

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.databinding.FragmentNewPostBinding
import com.example.artgallery.util.FileUtil
import com.example.artgallery.viewmodel.ForumViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for creating a new forum post
 */
class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ForumViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var photoFile: File? = null
    private var selectedCategory: String = ForumPost.CATEGORY_GENERAL
    
    // Register activity result launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoFile?.let { file ->
                selectedImageUri = Uri.fromFile(file)
                displaySelectedImage()
            }
        }
    }
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Copy the image to our app's private storage
                val context = requireContext()
                val destinationFile = FileUtil.createImageFile(context)
                FileUtil.copyImageToFile(context, uri, destinationFile)
                photoFile = destinationFile
                displaySelectedImage()
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCategorySelection()
        setupImageButtons()
        setupSubmitButton()
        setupCancelButton()
    }
    
    private fun setupCategorySelection() {
        // Set up category dropdown
        val categories = arrayOf(
            ForumPost.CATEGORY_GENERAL,
            ForumPost.CATEGORY_TECHNIQUES,
            ForumPost.CATEGORY_CRITIQUE,
            ForumPost.CATEGORY_EVENTS,
            ForumPost.CATEGORY_MARKETPLACE,
            ForumPost.CATEGORY_COLLABORATION
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories.map { it.capitalize() }
        )
        binding.spinnerCategory.adapter = adapter
        
        // Set default category
        binding.spinnerCategory.setSelection(0)
        
        // Set listener for category selection
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Default to general
                selectedCategory = ForumPost.CATEGORY_GENERAL
            }
        }
    }
    
    private fun setupImageButtons() {
        // Set up take photo button
        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()
        }
        
        // Set up choose from gallery button
        binding.buttonChooseImage.setOnClickListener {
            chooseFromGallery()
        }
        
        // Set up remove image button
        binding.buttonRemoveImage.setOnClickListener {
            removeSelectedImage()
        }
    }
    
    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val content = binding.editTextContent.text.toString().trim()
            val tags = binding.editTextTags.text.toString().trim()
            
            if (validateInput(title, content)) {
                createPost(title, content, tags)
            }
        }
    }
    
    private fun setupCancelButton() {
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun validateInput(title: String, content: String): Boolean {
        if (title.isEmpty()) {
            binding.editTextTitle.error = "Title cannot be empty"
            return false
        }
        
        if (content.isEmpty()) {
            binding.editTextContent.error = "Content cannot be empty"
            return false
        }
        
        return true
    }
    
    private fun createPost(title: String, content: String, tags: String) {
        // Create a new post
        val post = ForumPost(
            title = title,
            content = content,
            authorId = 1, // TODO: Replace with actual user ID
            authorName = "Current User", // TODO: Replace with actual username
            category = selectedCategory,
            createdAt = System.currentTimeMillis(),
            imagePath = photoFile?.absolutePath,
            tags = if (tags.isNotEmpty()) tags else null,
            pendingSyncToServer = true // Mark for syncing when online
        )
        
        // Save the post
        viewModel.createPost(post)
        
        // Show success message
        Snackbar.make(binding.root, "Post created successfully", Snackbar.LENGTH_SHORT).show()
        
        // Navigate back to forum
        findNavController().navigateUp()
    }
    
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // Create a file to save the image
        photoFile = FileUtil.createImageFile(requireContext())
        
        photoFile?.let { file ->
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.example.artgallery.fileprovider",
                file
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }
    
    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    
    private fun displaySelectedImage() {
        selectedImageUri?.let { uri ->
            binding.imagePreview.visibility = View.VISIBLE
            binding.buttonRemoveImage.visibility = View.VISIBLE
            
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.placeholder_post_image)
                .error(R.drawable.placeholder_post_image)
                .into(binding.imagePreview)
        }
    }
    
    private fun removeSelectedImage() {
        selectedImageUri = null
        photoFile?.delete()
        photoFile = null
        binding.imagePreview.visibility = View.GONE
        binding.buttonRemoveImage.visibility = View.GONE
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
