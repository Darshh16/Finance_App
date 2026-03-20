package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.personalfinanceapp.MainActivity
import com.example.personalfinanceapp.databinding.ActivitySplashBinding
import com.example.personalfinanceapp.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            delay(2000)
            when {
                // Not logged in — go to Login
                !sessionManager.isLoggedIn() -> {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                // Logged in + PIN enabled — go to PIN screen
                sessionManager.isPinEnabled() -> {
                    startActivity(Intent(this@SplashActivity, PinActivity::class.java))
                }
                // Logged in + no PIN — go to Dashboard
                else -> {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
            }
            finish()
        }
    }
}
