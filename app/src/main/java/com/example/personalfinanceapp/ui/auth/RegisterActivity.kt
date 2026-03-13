package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinanceapp.databinding.ActivityRegisterBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        // Register button click
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            // Basic validation
            if (name.isEmpty()) { binding.tilName.error = "Enter your name"; return@setOnClickListener }
            if (email.isEmpty()) { binding.tilEmail.error = "Enter your email"; return@setOnClickListener }
            if (password.isEmpty()) { binding.tilPassword.error = "Enter a password"; return@setOnClickListener }
            if (password.length < 6) { binding.tilPassword.error = "Password must be at least 6 characters"; return@setOnClickListener }
            if (password != confirm) { binding.tilConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }

            // Clear errors and show loading
            clearErrors()
            showLoading(true)
            viewModel.register(name, email, password)
        }

        // Navigate to Login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(this) { result ->
            showLoading(false)
            result.onSuccess { user ->
                sessionManager.saveSession(user.id, user.name)
                startActivity(Intent(this, com.example.personalfinanceapp.MainActivity::class.java))
                finish()

                // TODO: Navigate to Dashboard in Step 4
                // For now, go to Login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }
}
