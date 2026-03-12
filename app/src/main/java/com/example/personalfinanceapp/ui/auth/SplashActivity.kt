package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        // Wait 2 seconds then navigate
        lifecycleScope.launch {
            delay(2000)
            if (sessionManager.isLoggedIn()) {
                // TODO: Go to Dashboard (we build this in Step 4)
                goToLogin() // for now, go to login
            } else {
                goToLogin()
            }
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // remove splash from back stack
    }
}
