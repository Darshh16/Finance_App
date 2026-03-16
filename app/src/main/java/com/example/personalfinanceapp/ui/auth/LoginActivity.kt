package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.personalfinanceapp.MainActivity
import com.example.personalfinanceapp.databinding.ActivityLoginBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dark mode preference
        sessionManager = SessionManager(this)
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty()) { binding.tilEmail.error = "Enter your email"; return@setOnClickListener }
            if (password.isEmpty()) { binding.tilPassword.error = "Enter your password"; return@setOnClickListener }
            binding.tilEmail.error = null
            binding.tilPassword.error = null
            showLoading(true)
            viewModel.login(email, password)
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            showLoading(false)
            result.onSuccess { user ->
                // Save email to session too
                sessionManager.saveSession(user.id, user.name, user.email)
                Toast.makeText(this, "Welcome back ${user.name}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }
}
