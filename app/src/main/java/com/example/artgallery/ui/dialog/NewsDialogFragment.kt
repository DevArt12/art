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
import com.example.artgallery.data.entity.ArtNews
import com.example.artgallery.databinding.DialogNewsEditBinding
import com.example.artgallery.util.ImageUtils
import com.example.artgallery.viewmodel.ArtNewsViewModel
import androidx.core.os.bundleOf

class NewsDialogFragment : DialogFragment() {
    private var _binding: DialogNewsEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtNewsViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var news: ArtNews? = null
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        news = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("news", ArtNews::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<ArtNews>("news")
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(requireContext())
                    .load(uri)
                    .centerCrop()
                    .into(binding.ivNewsImage)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNewsEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        news?.let { news ->
            binding.apply {
                etTitle.setText(news.title)
                etContent.setText(news.content)
                
                Glide.with(requireContext())
                    .load(news.imagePath)
                    .centerCrop()
                    .into(ivNewsImage)
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
            saveNews()
        }
    }

    private fun saveNews() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()

        selectedImageUri?.let { uri ->
            imagePath = ImageUtils.saveImageToInternalStorage(requireContext(), uri)
        }

        val newNews = ArtNews(
            id = news?.id ?: 0,
            title = title,
            content = content,
            date = System.currentTimeMillis(),
            imagePath = imagePath ?: news?.imagePath ?: ""
        )

        if (news == null) {
            viewModel.insertNews(newNews)
        } else {
            viewModel.updateNews(newNews)
        }

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(news: ArtNews?): NewsDialogFragment {
            return NewsDialogFragment().apply {
                arguments = bundleOf("news" to news)
            }
        }
    }
}
