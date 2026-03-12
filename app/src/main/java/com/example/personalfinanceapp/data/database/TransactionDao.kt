package com.example.personalfinanceapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.personalfinanceapp.data.model.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // Get all transactions for a user, newest first
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: Int): LiveData<List<Transaction>>

    // Get only INCOME transactions
    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = 'INCOME' ORDER BY date DESC")
    fun getIncomeTransactions(userId: Int): LiveData<List<Transaction>>

    // Get only EXPENSE transactions
    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = 'EXPENSE' ORDER BY date DESC")
    fun getExpenseTransactions(userId: Int): LiveData<List<Transaction>>

    // Get total income amount
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: Int): LiveData<Double>

    // Get total expense amount
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpense(userId: Int): LiveData<Double>

    // Get transactions by category
    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(userId: Int, category: String): LiveData<List<Transaction>>

    // Get total spent in a category (for budget tracking)
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE'")
    suspend fun getTotalSpentByCategory(userId: Int, category: String): Double

    // Get transactions between two dates
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<List<Transaction>>
}
