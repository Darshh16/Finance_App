package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.User
import com.example.personalfinanceapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    // LiveData results — UI observes these
    private val _registerResult = MutableLiveData<Result<User>>()
    val registerResult: LiveData<Result<User>> = _registerResult

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    // Register a new user
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                // Check if email already exists
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    _registerResult.postValue(Result.failure(Exception("Email already registered")))
                    return@launch
                }
                // Create new user
                val user = User(name = name, email = email, password = password)
                val id = repository.insertUser(user)
                val savedUser = user.copy(id = id.toInt())
                _registerResult.postValue(Result.success(savedUser))
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            }
        }
    }

    // Login existing user
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(email, password)
                if (user != null) {
                    _loginResult.postValue(Result.success(user))
                } else {
                    _loginResult.postValue(Result.failure(Exception("Invalid email or password")))
                }
            } catch (e: Exception) {
                _loginResult.postValue(Result.failure(e))
            }
        }
    }
}
