package com.example.personalfinanceapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val type: String,            // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val description: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

// Category constants — use these everywhere in the app
object TransactionCategory {
    // Expense categories
    const val FOOD = "Food"
    const val TRANSPORT = "Transportation"
    const val SHOPPING = "Shopping"
    const val ENTERTAINMENT = "Entertainment"
    const val BILLS = "Bills"
    const val HEALTHCARE = "Healthcare"
    const val EDUCATION = "Education"
    const val OTHER = "Other"

    // Income categories
    const val SALARY = "Salary"
    const val FREELANCE = "Freelance"
    const val BUSINESS = "Business"
    const val GIFT = "Gift"
    const val INVESTMENT = "Investment"

    val expenseCategories = listOf(FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTHCARE, EDUCATION, OTHER)
    val incomeCategories = listOf(SALARY, FREELANCE, BUSINESS, GIFT, INVESTMENT, OTHER)
}
