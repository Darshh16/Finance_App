package com.example.personalfinanceapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.personalfinanceapp.R

object NotificationHelper {

    private const val CHANNEL_ID = "budget_alerts"
    private const val CHANNEL_NAME = "Budget Alerts"
    private var notificationId = 1000

    // Call this once when app starts — creates the notification channel
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you are close to or over your budget"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Send a warning when spending is 80%+ of budget
    fun sendNearLimitAlert(context: Context, category: String, spent: Double, limit: Double) {
        val percent = (spent / limit * 100).toInt()
        sendNotification(
            context = context,
            title = "⚠️ Budget Warning — $category",
            message = "You've used $percent% of your ₹${String.format("%.0f", limit)} $category budget.",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    // Send an urgent alert when over budget
    fun sendOverBudgetAlert(context: Context, category: String, spent: Double, limit: Double) {
        val over = spent - limit
        sendNotification(
            context = context,
            title = "🚨 Over Budget — $category!",
            message = "You're ₹${String.format("%.0f", over)} over your $category budget! Limit: ₹${String.format("%.0f", limit)}",
            priority = NotificationCompat.PRIORITY_MAX
        )
    }

    private fun sendNotification(context: Context, title: String, message: String, priority: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId++, notification)
    }
}
