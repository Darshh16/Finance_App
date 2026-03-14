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

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)
    }

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                _saveResult.postValue(true)
            } catch (e: Exception) {
                _saveResult.postValue(false)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun getAllTransactions(userId: Int): LiveData<List<Transaction>> =
        repository.getAllTransactions(userId)
}
