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
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.DialogArtworkEditBinding
import com.example.artgallery.util.ImageUtils
import com.example.artgallery.viewmodel.ArtworkViewModel
import androidx.core.os.bundleOf

class ArtworkEditDialogFragment : DialogFragment() {
    private var _binding: DialogArtworkEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtworkViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var artwork: Artwork? = null
    private var imagePath: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(requireContext())
                    .load(uri)
                    .into(binding.ivArtwork)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artwork = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("artwork", Artwork::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<Artwork>("artwork")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogArtworkEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        artwork?.let { artwork ->
            binding.apply {
                etTitle.setText(artwork.title)
                etDescription.setText(artwork.description)
                etPrice.setText(artwork.price.toString())
                
                Glide.with(requireContext())
                    .load(artwork.imagePath)
                    .into(ivArtwork)
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
            saveArtwork()
        }
    }

    private fun saveArtwork() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val priceStr = binding.etPrice.text.toString()
        val isForSale = true // Assuming this is always true for now
        val price = if (isForSale) priceStr.toDoubleOrNull() else null
        val contactDetails = "" // Assuming this is empty for now

        selectedImageUri?.let { uri ->
            imagePath = ImageUtils.saveImageToInternalStorage(requireContext(), uri)
        }

        val updatedArtwork = Artwork(
            id = artwork?.id ?: 0,
            title = title,
            description = description,
            imagePath = selectedImageUri?.toString() ?: artwork?.imagePath ?: "",
            artistId = artwork?.artistId ?: 0,
            category = artwork?.category ?: "",
            price = price,
            isForSale = isForSale,
            contactDetails = contactDetails
        )

        if (artwork == null) {
            viewModel.insertArtwork(updatedArtwork)
        } else {
            viewModel.updateArtwork(updatedArtwork)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(artwork: Artwork?): ArtworkEditDialogFragment {
            return ArtworkEditDialogFragment().apply {
                arguments = bundleOf("artwork" to artwork)
            }
        }
    }
}
