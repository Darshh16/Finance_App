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
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    // Save user session after login
    fun saveSession(userId: Int, userName: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Check if user is already logged in
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // Get saved user ID
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    // Get saved user name
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    // Clear session on logout
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
