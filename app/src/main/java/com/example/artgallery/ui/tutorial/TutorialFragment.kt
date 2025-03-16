package com.example.artgallery.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artgallery.R
import com.example.artgallery.adapter.TutorialAdapter
import com.example.artgallery.data.entity.Tutorial
import com.example.artgallery.databinding.FragmentTutorialBinding
import com.example.artgallery.viewmodel.TutorialViewModel
import com.google.android.material.snackbar.Snackbar

class TutorialFragment : Fragment() {
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TutorialViewModel by viewModels()
    private lateinit var adapter: TutorialAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupSearchFunctionality()

    }

    private fun setupRecyclerView() {
        adapter = TutorialAdapter(this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TutorialFragment.adapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.editTextSearch.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                viewModel.clearSearch()
            }
        }

        binding.editTextSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                if (query.isNotBlank()) {
                    viewModel.searchTutorials(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }


    private fun observeViewModel() {
        viewModel.filteredTutorials.observe(viewLifecycleOwner, Observer { tutorials ->
            adapter.submitList(tutorials)

            if (tutorials.isEmpty()) {
                binding.textNoResults.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.textNoResults.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }

            binding.progressBar.visibility = View.GONE
        })

        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { status ->
            status?.let {
                adapter.updateDownloadProgress(it.tutorialId, it.progress)
                if (it.progress == 100) {
                    Snackbar.make(binding.root, "Tutorial downloaded successfully", Snackbar.LENGTH_SHORT).show()
                    viewModel.clearDownloadStatus()
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        })
    }


    override fun onTutorialClick(tutorial: Tutorial) {
        val action = TutorialFragmentDirections.actionTutorialFragmentToTutorialDetailFragment(tutorial.id)
        findNavController().navigate(action)
        viewModel.incrementViewCount(tutorial.id)
    }

    override fun onDownloadClick(tutorial: Tutorial) {
        if (tutorial.isDownloaded) {
            Snackbar.make(binding.root, "Tutorial already downloaded", Snackbar.LENGTH_SHORT)
                .setAction("Delete") {
                    viewModel.deleteTutorialDownload(tutorial.id)
                }
                .show()
        } else {
            viewModel.downloadTutorial(tutorial.id)
            Snackbar.make(binding.root, "Downloading tutorial...", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}