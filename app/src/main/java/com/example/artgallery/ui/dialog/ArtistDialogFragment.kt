package com.example.artgallery.ui.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.artgallery.data.entity.Artist
import com.example.artgallery.databinding.DialogArtistEditBinding
import com.example.artgallery.util.ImageUtils
import com.example.artgallery.viewmodel.ArtistViewModel
import androidx.core.os.bundleOf

class ArtistDialogFragment : DialogFragment() {
    private var _binding: DialogArtistEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtistViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var artist: Artist? = null
    private var imagePath: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(requireContext())
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artist = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("artist", Artist::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("artist")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogArtistEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        artist?.let { artist ->
            binding.apply {
                etName.setText(artist.name)
                etBio.setText(artist.bio)
                
                Glide.with(requireContext())
                    .load(artist.profilePicturePath)
                    .circleCrop()
                    .into(ivProfilePicture)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            getContent.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            saveArtist()
        }
    }

    private fun saveArtist() {
        val name = binding.etName.text.toString()
        val bio = binding.etBio.text.toString()

        selectedImageUri?.let { uri ->
            imagePath = ImageUtils.saveImageToInternalStorage(requireContext(), uri)
        }

        val newArtist = Artist(
            id = artist?.id ?: 0,
            name = name,
            bio = bio,
            profilePicturePath = imagePath ?: artist?.profilePicturePath ?: ""
        )

        if (artist == null) {
            viewModel.insertArtist(newArtist)
        } else {
            viewModel.updateArtist(newArtist)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(artist: Artist?): ArtistDialogFragment {
            return ArtistDialogFragment().apply {
                arguments = bundleOf("artist" to artist)
            }
        }
    }
}
