package com.example.personalfinanceapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val category: String,
    val limitAmount: Double,
    val spentAmount: Double = 0.0,
    val month: Int,
    val year: Int
)
