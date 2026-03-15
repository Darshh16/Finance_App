package com.example.personalfinanceapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.Transaction
import com.example.personalfinanceapp.data.model.TransactionCategory
import com.example.personalfinanceapp.data.repository.FinanceRepository
import kotlinx.coroutines.launch

data class CategoryStat(val category: String, val amount: Double, val percent: Float)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _allTransactions = MutableLiveData<List<Transaction>>()
    val allTransactions: LiveData<List<Transaction>> = _allTransactions

    private val _categoryStats = MutableLiveData<List<CategoryStat>>()
    val categoryStats: LiveData<List<CategoryStat>> = _categoryStats

    private val _totalIncome = MutableLiveData<Double>(0.0)
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    fun loadReports(userId: Int) {
        repository.getAllTransactions(userId).observeForever { transactions ->
            _allTransactions.postValue(transactions)
            processData(transactions)
        }
    }

    private fun processData(transactions: List<Transaction>) {
        viewModelScope.launch {
            val income  = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            _totalIncome.postValue(income)
            _totalExpense.postValue(expense)

            // Build category stats from EXPENSE transactions only
            val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
            val grouped = expenseTransactions.groupBy { it.category }
            val totalExpenseAmt = expenseTransactions.sumOf { it.amount }

            val stats = grouped.map { (cat, txns) ->
                val amt = txns.sumOf { it.amount }
                val pct = if (totalExpenseAmt > 0) ((amt / totalExpenseAmt) * 100).toFloat() else 0f
                CategoryStat(cat, amt, pct)
            }.sortedByDescending { it.amount }

            _categoryStats.postValue(stats)
        }
    }
}
