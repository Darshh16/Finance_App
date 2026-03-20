package com.example.personalfinanceapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.personalfinanceapp.MainActivity
import com.example.personalfinanceapp.R
import com.example.personalfinanceapp.databinding.ActivityPinBinding
import com.example.personalfinanceapp.utils.SessionManager

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var sessionManager: SessionManager
    private var enteredPin = ""
    private var isSettingPin = false  // true = first time setup
    private var firstPin = ""         // stores first entry during setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        // Check if this is first time PIN setup
        isSettingPin = !sessionManager.isPinEnabled()
        if (isSettingPin) {
            binding.tvTitle.text = "Set Your PIN"
            binding.tvSubtitle.text = "Choose a 4-digit PIN to secure your app"
            binding.btnFingerprint.visibility = View.GONE
        } else {
            // Try fingerprint automatically on open
            tryFingerprint()
        }

        setupNumberButtons()
        binding.btnFingerprint.setOnClickListener { tryFingerprint() }
    }

    private fun setupNumberButtons() {
        val buttons = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        )
        buttons.forEach { (button, digit) ->
            button.setOnClickListener { addDigit(digit) }
        }
        binding.btnClear.setOnClickListener { removeDigit() }
        binding.btnOk.setOnClickListener { confirmPin() }
    }

    private fun addDigit(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin += digit
            updateDots()
            if (enteredPin.length == 4) confirmPin()
        }
    }

    private fun removeDigit() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            updateDots()
        }
    }

    private fun updateDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < enteredPin.length) R.drawable.pin_dot_filled
                else R.drawable.pin_dot_empty
            )
        }
    }

    private fun confirmPin() {
        if (enteredPin.length < 4) {
            showError("Please enter all 4 digits")
            return
        }
        if (isSettingPin) {
            if (firstPin.isEmpty()) {
                // First entry — ask to confirm
                firstPin = enteredPin
                enteredPin = ""
                updateDots()
                binding.tvTitle.text = "Confirm PIN"
                binding.tvSubtitle.text = "Enter your PIN again to confirm"
                binding.tvError.visibility = View.GONE
            } else {
                // Second entry — check match
                if (enteredPin == firstPin) {
                    sessionManager.setPin(enteredPin)
                    Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    showError("PINs do not match. Try again.")
                    firstPin = ""
                    enteredPin = ""
                    updateDots()
                    binding.tvTitle.text = "Set Your PIN"
                    binding.tvSubtitle.text = "Choose a 4-digit PIN to secure your app"
                }
            }
        } else {
            // Verify PIN
            if (enteredPin == sessionManager.getPin()) {
                goToMain()
            } else {
                showError("Incorrect PIN. Try again.")
                enteredPin = ""
                updateDots()
            }
        }
    }

    private fun tryFingerprint() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            != BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnFingerprint.visibility = View.GONE
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToMain()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Fall back to PIN — do nothing, let user type PIN
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showError("Fingerprint not recognized")
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Use your fingerprint to open the app")
            .setNegativeButtonText("Use PIN")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
