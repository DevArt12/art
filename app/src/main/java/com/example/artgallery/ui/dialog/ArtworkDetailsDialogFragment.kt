package com.example.artgallery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.DialogArtworkDetailsBinding
import androidx.core.os.bundleOf

class ArtworkDetailsDialogFragment : DialogFragment() {
    private var _binding: DialogArtworkDetailsBinding? = null
    private val binding get() = _binding!!
    private var artwork: Artwork? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artwork = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("artwork", Artwork::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("artwork")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogArtworkDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        artwork?.let { artwork ->
            binding.apply {
                tvTitle.text = artwork.title
                tvDescription.text = artwork.description
                tvPrice.text = String.format("$%.2f", artwork.price)
                
                Glide.with(requireContext())
                    .load(artwork.imagePath)
                    .into(ivArtwork)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(artwork: Artwork): ArtworkDetailsDialogFragment {
            return ArtworkDetailsDialogFragment().apply {
                arguments = bundleOf("artwork" to artwork)
            }
        }
    }
}
