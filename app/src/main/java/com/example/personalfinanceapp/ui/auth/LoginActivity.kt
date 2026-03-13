package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinanceapp.databinding.ActivityLoginBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
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
                sessionManager.saveSession(user.id, user.name)
                Toast.makeText(this, "Welcome back ${user.name}!", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Dashboard in Step 4
                startActivity(Intent(this, com.example.personalfinanceapp.MainActivity::class.java))
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
