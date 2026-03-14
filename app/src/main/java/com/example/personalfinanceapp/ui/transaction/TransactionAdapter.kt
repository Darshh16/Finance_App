package com.example.personalfinanceapp.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.model.Transaction
import com.example.personalfinanceapp.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvCategory.text = transaction.category
            binding.tvDescription.text = transaction.description.ifEmpty { transaction.type }
            binding.tvCategoryIcon.text = getCategoryEmoji(transaction.category)

            if (transaction.type == "INCOME") {
                binding.tvAmount.text = "+ ₹${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            } else {
                binding.tvAmount.text = "- ₹${String.format("%.2f", transaction.amount)}"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#C62828"))
            }

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(transaction.date))

            // Short tap — item click
            binding.root.setOnClickListener { onItemClick(transaction) }

            // Long press — show delete confirmation dialog
            binding.root.setOnLongClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this ${transaction.category} transaction of ₹${String.format("%.2f", transaction.amount)}?")
                    .setPositiveButton("Delete") { _, _ -> onDeleteClick(transaction) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    private fun getCategoryEmoji(category: String): String = when (category) {
        "Food" -> "🍔"
        "Transportation" -> "🚗"
        "Shopping" -> "🛍️"
        "Entertainment" -> "🎬"
        "Bills" -> "💡"
        "Healthcare" -> "🏥"
        "Education" -> "📚"
        "Salary" -> "💼"
        "Freelance" -> "💻"
        "Business" -> "📈"
        "Gift" -> "🎁"
        "Investment" -> "💰"
        else -> "💳"
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
        override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
    }
}
