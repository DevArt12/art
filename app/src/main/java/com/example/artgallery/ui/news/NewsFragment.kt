package com.example.artgallery.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artgallery.adapter.ArtNewsAdapter
import com.example.artgallery.databinding.FragmentNewsBinding
import com.example.artgallery.viewmodel.ArtNewsViewModel
import com.example.artgallery.ui.dialog.NewsDetailsDialogFragment
import com.example.artgallery.ui.dialog.NewsDialogFragment

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtNewsViewModel by viewModels()
    private lateinit var adapter: ArtNewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNews()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = ArtNewsAdapter(
            onItemClick = { news -> 
                // Show news details in a dialog
                val dialog = NewsDetailsDialogFragment.newInstance(news)
                dialog.show(childFragmentManager, "news_details")
            },
            onEditClick = { news ->
                val dialog = NewsDialogFragment.newInstance(news)
                dialog.show(childFragmentManager, "edit_news")
            },
            onDeleteClick = { news ->
                viewModel.deleteNews(news)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NewsFragment.adapter
        }
    }

    private fun observeNews() {
        viewModel.allNews.observe(viewLifecycleOwner) { newsList ->
            adapter.submitList(newsList)
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val dialog = NewsDialogFragment.newInstance(null)
            dialog.show(childFragmentManager, "add_news")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
