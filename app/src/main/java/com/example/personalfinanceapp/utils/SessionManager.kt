package com.example.personalfinanceapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FinanceAppSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_DARK_MODE = "darkMode"
    }

    fun saveSession(userId: Int, userName: String, userEmail: String = "") {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    // Dark mode
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    // ── PIN Lock ────────────────────────────────────────────────
    private const val KEY_PIN = "userPin"
    private const val KEY_PIN_ENABLED = "pinEnabled"

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
        prefs.edit().putBoolean(KEY_PIN_ENABLED, true).apply()
    }

    fun getPin(): String = prefs.getString(KEY_PIN, "") ?: ""

    fun isPinEnabled(): Boolean = prefs.getBoolean(KEY_PIN_ENABLED, false)

    fun clearPin() {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, false).apply()
    }


    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
