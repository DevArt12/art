package com.example.artgallery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.artgallery.data.entity.ArtNews
import com.example.artgallery.databinding.DialogNewsDetailsBinding
import androidx.core.os.bundleOf
import java.text.SimpleDateFormat
import java.util.*

class NewsDetailsDialogFragment : DialogFragment() {
    private var _binding: DialogNewsDetailsBinding? = null
    private val binding get() = _binding!!
    private var news: ArtNews? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        news = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("news", ArtNews::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<ArtNews>("news")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNewsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        news?.let { news ->
            binding.apply {
                tvTitle.text = news.title
                tvContent.text = news.content
                tvDate.text = formatDate(news.date)
                
                if (!news.imagePath.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(news.imagePath)
                        .into(ivNews)
                }
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(news: ArtNews): NewsDetailsDialogFragment {
            return NewsDetailsDialogFragment().apply {
                arguments = bundleOf("news" to news)
            }
        }
    }
}
