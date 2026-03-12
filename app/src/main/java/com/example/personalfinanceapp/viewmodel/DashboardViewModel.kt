package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.Transaction
import com.example.personalfinanceapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val _userId = MutableLiveData<Int>()

    lateinit var totalIncome: LiveData<Double>
    lateinit var totalExpense: LiveData<Double>
    lateinit var recentTransactions: LiveData<List<Transaction>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    // Call this right after ViewModel is created, passing the logged-in user ID
    fun loadData(userId: Int) {
        totalIncome = repository.getTotalIncome(userId)
        totalExpense = repository.getTotalExpense(userId)
        recentTransactions = repository.getAllTransactions(userId)
    }

    // Calculate balance = income - expense
    fun getBalance(income: Double, expense: Double): Double = income - expense

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
