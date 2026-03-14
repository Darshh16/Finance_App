package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.Budget
import com.example.personalfinanceapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    fun getBudgetsForCurrentMonth(userId: Int): LiveData<List<Budget>> {
        return repository.getBudgetsByMonth(userId, currentMonth, currentYear)
    }

    fun saveBudget(userId: Int, category: String, limitAmount: Double) {
        viewModelScope.launch {
            try {
                // Check if budget already exists for this category this month
                val existing = repository.getBudgetByCategory(
                    userId, category, currentMonth, currentYear
                )
                if (existing != null) {
                    // Update existing budget limit
                    val updated = existing.copy(limitAmount = limitAmount)
                    repository.updateBudget(updated)
                } else {
                    // Create new budget
                    val budget = Budget(
                        userId = userId,
                        category = category,
                        limitAmount = limitAmount,
                        month = currentMonth,
                        year = currentYear
                    )
                    repository.insertBudget(budget)
                }
                _saveResult.postValue(true)
            } catch (e: Exception) {
                _saveResult.postValue(false)
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.updateBudget(budget.copy(limitAmount = 0.0))
        }
    }

    // Sync spent amounts from actual transactions into budgets
    fun syncSpentAmounts(userId: Int, budgets: List<Budget>) {
        viewModelScope.launch {
            budgets.forEach { budget ->
                val spent = repository.getTotalSpentByCategory(userId, budget.category)
                if (spent != budget.spentAmount) {
                    repository.updateBudget(budget.copy(spentAmount = spent))
                }
            }
        }
    }
}
