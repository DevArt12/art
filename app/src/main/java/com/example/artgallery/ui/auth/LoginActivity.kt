package com.example.artgallery.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.artgallery.MainActivity
import com.example.artgallery.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    
    companion object {
        private const val TAG = "LoginActivity"
        private const val WEB_CLIENT_ID = "405222288981-ovd48lbbviojgt1u0bptebtrcc74ks6u.apps.googleusercontent.com"
    }

    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-in result received: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleGoogleSignInResult(account)
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed code=${e.statusCode}", e)
            val message = when (e.statusCode) {
                7 -> "Network error. Please check your internet connection"
                12500 -> "Google Play Services is missing or outdated"
                12501 -> "Sign-in cancelled"
                12502 -> "Problem with your Google Account"
                else -> "Google sign in failed: ${e.statusCode}"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()
        setupClickListeners()

        // Check if user is already signed in
        auth.currentUser?.let {
            Log.d(TAG, "User already signed in: ${it.email}")
            navigateToMain()
            return
        }
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            
            // Check for existing Google Sign In account
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                Log.d(TAG, "Found existing Google Sign In account")
                handleGoogleSignInResult(account)
            }
            
            Log.d(TAG, "Google Sign In configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Google Sign In", e)
            Toast.makeText(this, "Error setting up Google Sign In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        Log.d(TAG, "Google Sign In successful. Email: ${account.email}")
        account.idToken?.let { token ->
            firebaseAuthWithGoogle(token)
        } ?: run {
            Log.e(TAG, "No ID token found in Google Sign In result")
            Toast.makeText(this, "Failed to get Google credentials", Toast.LENGTH_SHORT).show()
            googleSignInClient.signOut() // Sign out to retry
        }
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmailPassword(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val email = binding.emailEditText.text.toString()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email address to receive a password reset link")
            .setView(layoutInflater.inflate(com.example.artgallery.R.layout.dialog_forgot_password, null))
            .setPositiveButton("Send") { dialog, _ ->
                val dialogView = (dialog as androidx.appcompat.app.AlertDialog).findViewById<com.google.android.material.textfield.TextInputEditText>(com.example.artgallery.R.id.resetEmailEditText)
                val resetEmail = dialogView?.text?.toString() ?: email
                
                if (resetEmail.isNotEmpty()) {
                    sendPasswordResetEmail(resetEmail)
                } else {
                    Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Error sending reset email", task.exception)
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    navigateToMain()
                } else {
                    Log.e(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign In flow")
        try {
            // Sign out first to ensure clean state
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Google Sign In", e)
            Toast.makeText(this, "Error starting Google Sign In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "firebaseAuthWithGoogle: starting")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    navigateToMain()
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    // Sign out on failure to ensure clean state
                    googleSignInClient.signOut()
                }
            }
    }

    private fun navigateToMain() {
        Log.d(TAG, "Navigating to MainActivity")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
