package com.example.personalfinanceapp.utils

import com.example.personalfinanceapp.data.model.Transaction

object InsightsHelper {

    // Generates a list of insight messages based on transactions
    fun generateInsights(transactions: List<Transaction>): List<String> {
        val insights = mutableListOf<String>()
        if (transactions.isEmpty()) {
            insights.add("💡 Add your first transaction to see spending insights!")
            return insights
        }

        val expenses = transactions.filter { it.type == "EXPENSE" }
        val income = transactions.filter { it.type == "INCOME" }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = income.sumOf { it.amount }

        // Insight 1 — Savings rate
        if (totalIncome > 0) {
            val savingsRate = ((totalIncome - totalExpense) / totalIncome * 100).toInt()
            when {
                savingsRate >= 30 -> insights.add("⭐ Great job! You're saving ${savingsRate}% of your income.")
                savingsRate >= 10 -> insights.add("💡 You're saving ${savingsRate}% of your income. Try to reach 30%.")
                savingsRate < 0 -> insights.add("⚠️ You're spending more than you earn! Review your expenses.")
                else -> insights.add("📊 You're saving only ${savingsRate}% of income. Aim for at least 20%.")
            }
        }

        // Insight 2 — Top spending category
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
        val topCategory = categoryTotals.maxByOrNull { it.value }
        if (topCategory != null && totalExpense > 0) {
            val percent = (topCategory.value / totalExpense * 100).toInt()
            insights.add("🏆 Your biggest expense is ${topCategory.key} at ${percent}% of total spending.")
        }

        // Insight 3 — Food spending warning
        val foodSpending = categoryTotals["Food"] ?: 0.0
        if (totalExpense > 0 && foodSpending / totalExpense > 0.35) {
            insights.add("🍔 Food spending is over 35% of expenses. Consider meal prepping to save money.")
        }

        // Insight 4 — Transaction frequency
        if (expenses.size > 20) {
            insights.add("📈 You've made ${expenses.size} expense transactions. Track them to spot patterns.")
        }

        // Insight 5 — Balance warning
        val balance = totalIncome - totalExpense
        if (balance < 0) {
            insights.add("🚨 Your balance is negative (₹${String.format("%.0f", -balance)}). Reduce spending immediately.")
        } else if (balance < 1000) {
            insights.add("⚠️ Your balance is low (₹${String.format("%.0f", balance)}). Be careful with spending.")
        }

        // Insight 6 — Entertainment tip
        val entertainmentSpending = categoryTotals["Entertainment"] ?: 0.0
        if (entertainmentSpending > 2000) {
            insights.add("🎬 You spent ₹${String.format("%.0f", entertainmentSpending)} on Entertainment. Look for free alternatives.")
        }

        return insights.take(3) // Show max 3 insights
    }
}
