package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.SavingsGoal
import com.example.personalfinanceapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    fun getAllGoals(userId: Int): LiveData<List<SavingsGoal>> =
        repository.getAllGoals(userId)

    fun getActiveGoals(userId: Int): LiveData<List<SavingsGoal>> =
        repository.getActiveGoals(userId)

    fun createGoal(userId: Int, title: String, targetAmount: Double, deadline: Long?) {
        viewModelScope.launch {
            try {
                val goal = SavingsGoal(
                    userId = userId,
                    title = title,
                    targetAmount = targetAmount,
                    deadline = deadline
                )
                repository.insertGoal(goal)
                _saveResult.postValue(true)
            } catch (e: Exception) {
                _saveResult.postValue(false)
            }
        }
    }

    fun addSavings(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val newAmount = goal.savedAmount + amount
            val isCompleted = newAmount >= goal.targetAmount
            val updated = goal.copy(
                savedAmount = newAmount.coerceAtMost(goal.targetAmount),
                isCompleted = isCompleted
            )
            repository.updateGoal(updated)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun markComplete(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateGoal(
                goal.copy(savedAmount = goal.targetAmount, isCompleted = true)
            )
        }
    }
}
