package com.example.personalfinanceapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.personalfinanceapp.data.model.SavingsGoal

@Dao
interface SavingsGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    // Get all goals for a user
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoals(userId: Int): LiveData<List<SavingsGoal>>

    // Get only active (not completed) goals
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND isCompleted = 0")
    fun getActiveGoals(userId: Int): LiveData<List<SavingsGoal>>

    // Update saved amount
    @Query("UPDATE savings_goals SET savedAmount = :amount WHERE id = :goalId")
    suspend fun updateSavedAmount(goalId: Int, amount: Double)
}
