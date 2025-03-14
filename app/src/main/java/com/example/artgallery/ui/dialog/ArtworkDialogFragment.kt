package com.example.artgallery.ui.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.DialogArtworkEditBinding
import com.example.artgallery.util.ImageUtils
import com.example.artgallery.viewmodel.ArtworkViewModel

class ArtworkDialogFragment : DialogFragment() {
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
                    .centerCrop()
                    .into(binding.ivArtwork)
            }
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
                cbForSale.isChecked = artwork.isForSale
                etPrice.setText(artwork.price?.toString() ?: "")
                etContactDetails.setText(artwork.contactDetails)
                
                Glide.with(requireContext())
                    .load(artwork.imagePath)
                    .centerCrop()
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

        binding.cbForSale.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPrice.isVisible = isChecked
            binding.tilContactDetails.isVisible = isChecked
        }
    }

    fun saveArtwork(artistId: Long) {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val isForSale = binding.cbForSale.isChecked
        val priceStr = binding.etPrice.text.toString()
        val price = if (isForSale) priceStr.toDoubleOrNull() else null
        val contactDetails = if (isForSale) binding.etContactDetails.text.toString() else null

        selectedImageUri?.let { uri ->
            imagePath = ImageUtils.saveImageToInternalStorage(requireContext(), uri)
        }

        val newArtwork = Artwork(
            id = artwork?.id ?: 0,
            title = title,
            description = description,
            imagePath = imagePath ?: artwork?.imagePath ?: "",
            artistId = artistId,
            category = artwork?.category ?: Artwork.CATEGORY_PAINTING,
            isForSale = isForSale,
            price = price,
            contactDetails = contactDetails
        )

        if (artwork == null) {
            viewModel.insertArtwork(newArtwork)
        } else {
            viewModel.updateArtwork(newArtwork)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(artwork: Artwork? = null): ArtworkDialogFragment {
            return ArtworkDialogFragment().apply {
                this.artwork = artwork
                this.imagePath = artwork?.imagePath
            }
        }
    }
}
