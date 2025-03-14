package com.example.artgallery.ui.forum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.artgallery.R
import com.example.artgallery.adapter.CommentAdapter
import com.example.artgallery.data.entity.ForumComment
import com.example.artgallery.data.entity.ForumPost
import com.example.artgallery.databinding.FragmentPostDetailBinding
import com.example.artgallery.viewmodel.ForumViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment for viewing a forum post and its comments
 */
class PostDetailFragment : Fragment(), CommentAdapter.CommentClickListener {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ForumViewModel by viewModels()
    private val args: PostDetailFragmentArgs by navArgs()
    
    private lateinit var commentAdapter: CommentAdapter
    private var currentPost: ForumPost? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupMenuProvider()
        
        // Load post details
        loadPostDetails(args.postId)
        
        // Set up comment submission
        binding.buttonSubmitComment.setOnClickListener {
            submitComment()
        }
        
        // Set up like button
        binding.buttonLike.setOnClickListener {
            currentPost?.let { post ->
                viewModel.toggleLike(post.id)
            }
        }
        
        // Observe liked posts
        viewModel.likedPostIds.observe(viewLifecycleOwner, Observer { likedIds ->
            updateLikeButton(likedIds)
        })
    }
    
    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(this)
        binding.recyclerViewComments.adapter = commentAdapter
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupMenuProvider() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_post_detail, menu)
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        currentPost?.let { post ->
                            val action = PostDetailFragmentDirections.actionPostDetailFragmentToEditPostFragment(post.id)
                            findNavController().navigate(action)
                        }
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    R.id.action_share -> {
                        sharePost()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun loadPostDetails(postId: Long) {
        // Load post details
        viewModel.getPostById(postId).observe(viewLifecycleOwner, Observer { post ->
            post?.let {
                currentPost = it
                displayPostDetails(it)
                
                // Load comments for this post
                loadComments(it.id)
            } ?: run {
                // Post not found
                Snackbar.make(binding.root, "Post not found", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        })
    }
    
    private fun displayPostDetails(post: ForumPost) {
        binding.textTitle.text = post.title
        binding.textContent.text = post.content
        binding.textAuthor.text = post.authorName
        binding.textCategory.text = post.category.capitalize()
        binding.textCommentCount.text = "${post.commentCount} comments"
        binding.textLikeCount.text = "${post.likeCount}"
        binding.textViewCount.text = "${post.viewCount} views"
        
        // Format and display date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        binding.textDate.text = dateFormat.format(Date(post.createdAt))
        
        // Show edited indicator if post was edited
        if (post.lastEditedAt != null) {
            binding.textEdited.visibility = View.VISIBLE
            binding.textEdited.text = "Edited ${viewModel.formatRelativeTime(post.lastEditedAt)}"
        } else {
            binding.textEdited.visibility = View.GONE
        }
        
        // Load post image if available
        if (post.imagePath != null && post.imagePath.isNotEmpty()) {
            binding.imagePost.visibility = View.VISIBLE
            val imageFile = File(post.imagePath)
            if (imageFile.exists()) {
                // Load from local file
                Glide.with(binding.imagePost)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_post_image)
                    .error(R.drawable.placeholder_post_image)
                    .into(binding.imagePost)
            } else {
                // Load from URL or resource
                Glide.with(binding.imagePost)
                    .load(post.imagePath)
                    .placeholder(R.drawable.placeholder_post_image)
                    .error(R.drawable.placeholder_post_image)
                    .into(binding.imagePost)
            }
        } else {
            binding.imagePost.visibility = View.GONE
        }
        
        // Show tags if available
        if (post.tags != null && post.tags.isNotEmpty()) {
            binding.textTags.visibility = View.VISIBLE
            binding.textTags.text = post.tags.replace(",", " • ")
        } else {
            binding.textTags.visibility = View.GONE
        }
    }
    
    private fun loadComments(postId: Long) {
        viewModel.getCommentsForPost(postId).observe(viewLifecycleOwner) { comments ->
            if (comments.isEmpty()) {
                binding.textNoComments.visibility = View.VISIBLE
                binding.recyclerViewComments.visibility = View.GONE
            } else {
                binding.textNoComments.visibility = View.GONE
                binding.recyclerViewComments.visibility = View.VISIBLE
                commentAdapter.submitList(comments)
            }
        }
    }
    
    private fun submitComment() {
        val commentText = binding.editTextComment.text.toString().trim()
        if (commentText.isEmpty()) {
            Snackbar.make(binding.root, "Comment cannot be empty", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        currentPost?.let { post ->
            val comment = ForumComment(
                id = 0, // Room will generate ID
                postId = post.id,
                content = commentText,
                authorId = viewModel.getCurrentUserId(),
                authorName = viewModel.getCurrentUserName(),
                createdAt = System.currentTimeMillis(),
                isEdited = false
            )
            
            viewModel.addComment(comment).observe(viewLifecycleOwner) { success ->
                if (success) {
                    binding.editTextComment.text?.clear()
                    // Hide keyboard
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(binding.editTextComment.windowToken, 0)
                } else {
                    Snackbar.make(binding.root, "Failed to add comment", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateLikeButton(likedIds: Set<Long>) {
        currentPost?.let { post ->
            val isLiked = likedIds.contains(post.id)
            
            if (isLiked) {
                binding.buttonLike.setIconResource(R.drawable.ic_favorite)
                binding.buttonLike.setIconTintResource(R.color.colorLiked)
            } else {
                binding.buttonLike.setIconResource(R.drawable.ic_favorite_border)
                binding.buttonLike.setIconTintResource(R.color.colorUnliked)
            }
        }
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                currentPost?.let { post ->
                    viewModel.deletePost(post.id)
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun sharePost() {
        currentPost?.let { post ->
            // In a real app, we would create a shareable link
            Snackbar.make(binding.root, "Sharing functionality will be implemented soon", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    override fun onCommentClick(comment: ForumComment) {
        if (comment.authorId == viewModel.getCurrentUserId()) {
            showCommentOptions(comment)
        }
    }
    
    private fun showCommentOptions(comment: ForumComment) {
        val options = arrayOf("Edit", "Delete")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Comment Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditCommentDialog(comment)
                    1 -> showDeleteCommentConfirmation(comment)
                }
            }
            .show()
    }
    
    private fun showEditCommentDialog(comment: ForumComment) {
        val editText = com.google.android.material.textfield.TextInputEditText(requireContext())
        editText.setText(comment.content)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val updatedContent = editText.text.toString().trim()
                if (updatedContent.isNotEmpty()) {
                    val updatedComment = comment.copy(
                        content = updatedContent,
                        isEdited = true
                    )
                    viewModel.updateComment(updatedComment)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteCommentConfirmation(comment: ForumComment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteComment(comment.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
