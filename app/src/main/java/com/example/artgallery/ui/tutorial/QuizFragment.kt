package com.example.artgallery.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.artgallery.R
import com.example.artgallery.databinding.FragmentQuizBinding
import com.example.artgallery.viewmodel.TutorialViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment for taking quizzes related to tutorials
 */
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TutorialViewModel by viewModels()
    private val args: QuizFragmentArgs by navArgs()
    
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var totalQuestions = 0
    private lateinit var quizQuestions: List<QuizQuestion>
    
    private lateinit var optionA: View
    private lateinit var optionB: View
    private lateinit var optionC: View
    private lateinit var optionD: View
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Load tutorial details
        viewModel.getTutorialById(args.tutorialId).observe(viewLifecycleOwner, Observer { tutorial ->
            tutorial?.let {
                binding.toolbar.title = "Quiz: ${it.title}"
                
                // If quiz is already completed, show completion message
                if (it.hasCompletedQuiz) {
                    showQuizAlreadyCompletedDialog()
                } else {
                    // Load quiz questions for this tutorial
                    loadQuizQuestions(it.id)
                }
            } ?: run {
                // Tutorial not found
                Snackbar.make(binding.root, "Tutorial not found", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        })
        
        // Set up answer option click listeners
        setupUI()
        
        // Set up next button
        binding.buttonNext.setOnClickListener {
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++
                displayCurrentQuestion()
            } else {
                // Quiz completed
                showQuizResults()
            }
        }
    }
    
    private fun setupUI() {
        // Initialize option views
        optionA = binding.textOption1
        optionB = binding.textOption2
        optionC = binding.textOption3
        optionD = binding.textOption4

        // Set click listeners for options
        optionA.setOnClickListener { handleOptionClick(0) }
        optionB.setOnClickListener { handleOptionClick(1) }
        optionC.setOnClickListener { handleOptionClick(2) }
        optionD.setOnClickListener { handleOptionClick(3) }
    }
    
    private fun updateUI(question: QuizQuestion) {
        binding.progressQuiz.progress = currentQuestionIndex * 100 / totalQuestions

        optionA.isEnabled = true
        optionB.isEnabled = true
        optionC.isEnabled = true
        optionD.isEnabled = true

        optionA.setBackgroundResource(R.drawable.bg_quiz_option)
        optionB.setBackgroundResource(R.drawable.bg_quiz_option)
        optionC.setBackgroundResource(R.drawable.bg_quiz_option)
        optionD.setBackgroundResource(R.drawable.bg_quiz_option)

        optionA.text = question.options[0]
        optionB.text = question.options[1]
        optionC.text = question.options[2]
        optionD.text = question.options[3]
    }
    
    private fun handleOptionClick(selectedOption: Int) {
        val options = listOf(optionA, optionB, optionC, optionD)
        
        // Disable all options
        options.forEach { it.isEnabled = false }
        
        // Get current question
        val question = quizQuestions[currentQuestionIndex]
        
        // Check if answer is correct
        if (selectedOption == question.correctAnswerIndex) {
            // Show correct answer feedback
            options[selectedOption].setBackgroundResource(R.drawable.bg_quiz_option_correct)
            correctAnswers++
        } else {
            // Show incorrect answer feedback
            options[selectedOption].setBackgroundResource(R.drawable.bg_quiz_option_incorrect)
            options[question.correctAnswerIndex].setBackgroundResource(R.drawable.bg_quiz_option_correct)
        }
        
        // Show explanation
        binding.textExplanation.text = question.explanation
        binding.textExplanation.visibility = View.VISIBLE
        
        // Show next button
        binding.buttonNext.visibility = View.VISIBLE
    }
    
    private fun loadQuizQuestions(tutorialId: Long) {
        // In a real app, these would come from a database
        // For now, we'll use some sample questions based on the tutorial ID
        quizQuestions = when (tutorialId % 3) {
            0L -> getSamplePaintingQuestions()
            1L -> getSampleDrawingQuestions()
            else -> getSampleSculptureQuestions()
        }
        
        totalQuestions = quizQuestions.size
        currentQuestionIndex = 0
        correctAnswers = 0
        
        // Display first question
        displayCurrentQuestion()
    }
    
    private fun displayCurrentQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        
        // Update question text and progress
        binding.textQuestion.text = question.questionText
        binding.textQuestionNumber.text = "Question ${currentQuestionIndex + 1} of $totalQuestions"
        binding.progressBar.progress = ((currentQuestionIndex + 1) * 100) / totalQuestions
        
        // Update options
        updateUI(question)
        
        // Reset option states
        binding.optionA.isEnabled = true
        binding.optionB.isEnabled = true
        binding.optionC.isEnabled = true
        binding.optionD.isEnabled = true
        
        // Update next button
        binding.buttonNext.isEnabled = false
        binding.buttonNext.text = if (currentQuestionIndex < totalQuestions - 1) {
            getString(R.string.next_question)
        } else {
            getString(R.string.finish_quiz)
        }
    }
    
    private fun showQuizResults() {
        val score = (correctAnswers * 100) / totalQuestions
        val isPassed = score >= 70 // 70% passing score
        
        val message = if (isPassed) {
            "Congratulations! You passed the quiz with a score of $score%."
        } else {
            "You scored $score%. You need at least 70% to pass. Try again!"
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isPassed) "Quiz Passed!" else "Quiz Failed")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (isPassed) {
                    // Mark quiz as completed
                    viewModel.markQuizCompleted(args.tutorialId)
                    Snackbar.make(binding.root, "Quiz completed successfully!", Snackbar.LENGTH_SHORT).show()
                }
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showQuizAlreadyCompletedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quiz Already Completed")
            .setMessage("You have already completed this quiz successfully. Would you like to take it again for practice?")
            .setPositiveButton("Yes") { _, _ ->
                // Load quiz questions for practice
                loadQuizQuestions(args.tutorialId)
            }
            .setNegativeButton("No") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun getSamplePaintingQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                "Which of the following is NOT a primary color in painting?",
                listOf("Red", "Blue", "Green", "Yellow"),
                2
            ),
            QuizQuestion(
                "What technique involves applying thin, transparent layers of paint?",
                listOf("Impasto", "Glazing", "Stippling", "Sgraffito"),
                1
            ),
            QuizQuestion(
                "Which painting medium dries the slowest?",
                listOf("Acrylic", "Watercolor", "Oil", "Gouache"),
                2
            ),
            QuizQuestion(
                "What is the term for the contrast between light and dark in a painting?",
                listOf("Chiaroscuro", "Tenebrism", "Sfumato", "Imprimatura"),
                0
            ),
            QuizQuestion(
                "Which brush is best for detailed work?",
                listOf("Flat brush", "Round brush", "Fan brush", "Filbert brush"),
                1
            )
        )
    }
    
    private fun getSampleDrawingQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                "Which pencil grade is the softest?",
                listOf("2H", "HB", "2B", "6B"),
                3
            ),
            QuizQuestion(
                "What is the technique of using small, closely spaced dots called?",
                listOf("Hatching", "Stippling", "Scumbling", "Blending"),
                1
            ),
            QuizQuestion(
                "Which of these is NOT a common drawing medium?",
                listOf("Charcoal", "Graphite", "Conte", "Gouache"),
                3
            ),
            QuizQuestion(
                "What is the term for drawing from observation of real objects?",
                listOf("Life drawing", "Still life", "Plein air", "Gesture drawing"),
                0
            ),
            QuizQuestion(
                "Which technique uses parallel lines to create shading?",
                listOf("Cross-hatching", "Hatching", "Scribbling", "Pointillism"),
                1
            )
        )
    }
    
    private fun getSampleSculptureQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                "Which of these is a subtractive sculpting technique?",
                listOf("Modeling", "Casting", "Carving", "Assembling"),
                2
            ),
            QuizQuestion(
                "What material is traditionally used for lost-wax casting?",
                listOf("Clay", "Stone", "Bronze", "Wood"),
                2
            ),
            QuizQuestion(
                "Which tool is NOT typically used in stone carving?",
                listOf("Chisel", "Rasp", "Potter's wheel", "Point"),
                2
            ),
            QuizQuestion(
                "What is the term for a sculpture that is attached to a flat background?",
                listOf("Relief", "Kinetic", "Installation", "Mobile"),
                0
            ),
            QuizQuestion(
                "Which of these sculptors is known for mobile sculptures?",
                listOf("Auguste Rodin", "Alexander Calder", "Henry Moore", "Louise Bourgeois"),
                1
            )
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Data class representing a quiz question
     */
    data class QuizQuestion(
        val questionText: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )
}
