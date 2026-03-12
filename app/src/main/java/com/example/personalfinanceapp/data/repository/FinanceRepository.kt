package com.example.personalfinanceapp.data.repository

import androidx.lifecycle.LiveData
import com.example.personalfinanceapp.data.database.AppDatabase
import com.example.personalfinanceapp.data.model.*

class FinanceRepository(private val database: AppDatabase) {

    // ── User ────────────────────────────────────────────────────────
    suspend fun insertUser(user: User): Long =
        database.userDao().insertUser(user)

    suspend fun login(email: String, password: String): User? =
        database.userDao().login(email, password)

    suspend fun getUserByEmail(email: String): User? =
        database.userDao().getUserByEmail(email)

    suspend fun getUserById(userId: Int): User? =
        database.userDao().getUserById(userId)

    // ── Transactions ─────────────────────────────────────────────────
    suspend fun insertTransaction(transaction: Transaction): Long =
        database.transactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        database.transactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        database.transactionDao().deleteTransaction(transaction)

    fun getAllTransactions(userId: Int): LiveData<List<Transaction>> =
        database.transactionDao().getAllTransactions(userId)

    fun getTotalIncome(userId: Int): LiveData<Double> =
        database.transactionDao().getTotalIncome(userId)

    fun getTotalExpense(userId: Int): LiveData<Double> =
        database.transactionDao().getTotalExpense(userId)

    suspend fun getTotalSpentByCategory(userId: Int, category: String): Double =
        database.transactionDao().getTotalSpentByCategory(userId, category)

    fun getTransactionsByDateRange(userId: Int, start: Long, end: Long): LiveData<List<Transaction>> =
        database.transactionDao().getTransactionsByDateRange(userId, start, end)

    // ── Budgets ──────────────────────────────────────────────────────
    suspend fun insertBudget(budget: Budget): Long =
        database.budgetDao().insertBudget(budget)

    suspend fun updateBudget(budget: Budget) =
        database.budgetDao().updateBudget(budget)

    fun getBudgetsByMonth(userId: Int, month: Int, year: Int): LiveData<List<Budget>> =
        database.budgetDao().getBudgetsByMonth(userId, month, year)

    suspend fun getBudgetByCategory(userId: Int, category: String, month: Int, year: Int): Budget? =
        database.budgetDao().getBudgetByCategory(userId, category, month, year)

    // ── Savings Goals ────────────────────────────────────────────────
    suspend fun insertGoal(goal: SavingsGoal): Long =
        database.savingsGoalDao().insertGoal(goal)

    suspend fun updateGoal(goal: SavingsGoal) =
        database.savingsGoalDao().updateGoal(goal)

    suspend fun deleteGoal(goal: SavingsGoal) =
        database.savingsGoalDao().deleteGoal(goal)

    fun getAllGoals(userId: Int): LiveData<List<SavingsGoal>> =
        database.savingsGoalDao().getAllGoals(userId)

    fun getActiveGoals(userId: Int): LiveData<List<SavingsGoal>> =
        database.savingsGoalDao().getActiveGoals(userId)

    suspend fun updateSavedAmount(goalId: Int, amount: Double) =
        database.savingsGoalDao().updateSavedAmount(goalId, amount)
}
