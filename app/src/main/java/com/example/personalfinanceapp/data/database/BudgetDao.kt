package com.example.personalfinanceapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.personalfinanceapp.data.model.Budget

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    // Get all budgets for a specific month/year
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getBudgetsByMonth(userId: Int, month: Int, year: Int): LiveData<List<Budget>>

    // Get budget for a specific category
    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetByCategory(userId: Int, category: String, month: Int, year: Int): Budget?

    // Update spent amount for a budget
    @Query("UPDATE budgets SET spentAmount = :spent WHERE userId = :userId AND category = :category AND month = :month AND year = :year")
    suspend fun updateSpentAmount(userId: Int, category: String, month: Int, year: Int, spent: Double)
}
