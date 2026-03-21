package com.example.personalfinanceapp.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.model.Budget
import com.example.personalfinanceapp.data.model.TransactionCategory
import com.example.personalfinanceapp.databinding.DialogAddBudgetBinding
import com.example.personalfinanceapp.databinding.FragmentBudgetBinding
import com.example.personalfinanceapp.databinding.ItemBudgetBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import com.example.personalfinanceapp.R
import java.util.Locale

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = sdf.format(Calendar.getInstance().time)

        binding.rvBudgets.layoutManager = LinearLayoutManager(requireContext())

        // This FAB is INSIDE the fragment layout — not the main activity FAB
        binding.fabAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }

        loadBudgets()
        observeSaveResult()
    }

    private fun loadBudgets() {
        val userId = sessionManager.getUserId()
        viewModel.getBudgetsForCurrentMonth(userId).observe(viewLifecycleOwner) { budgets ->
            viewModel.syncSpentAmounts(requireContext(), userId, budgets)
            if (budgets.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvBudgets.visibility = View.GONE
                binding.tvTotalBudget.text = "₹0.00"
                binding.tvTotalSpent.text = "₹0.00"
                binding.tvTotalRemaining.text = "₹0.00"
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvBudgets.visibility = View.VISIBLE
                updateSummary(budgets)
                binding.rvBudgets.adapter = BudgetAdapter(budgets)
            }
        }
    }

    private fun updateSummary(budgets: List<Budget>) {
        val totalLimit   = budgets.sumOf { it.limitAmount }
        val totalSpent   = budgets.sumOf { it.spentAmount }
        val totalRemain  = totalLimit - totalSpent
        binding.tvTotalBudget.text    = "₹${String.format("%.2f", totalLimit)}"
        binding.tvTotalSpent.text     = "₹${String.format("%.2f", totalSpent)}"
        binding.tvTotalRemaining.text = "₹${String.format("%.2f", totalRemain)}"
        binding.tvTotalRemaining.setTextColor(
            if (totalRemain >= 0) android.graphics.Color.parseColor("#2E7D32")
            else android.graphics.Color.parseColor("#C62828")
        )
    }

    private fun showAddBudgetDialog() {
        val dialogBinding = DialogAddBudgetBinding.inflate(layoutInflater)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TransactionCategory.expenseCategories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialog)
            .setTitle("Set Monthly Budget")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val category = dialogBinding.spinnerCategory.selectedItem?.toString() ?: ""
                val amountStr = dialogBinding.etBudgetAmount.text.toString().trim()
                if (amountStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.saveBudget(sessionManager.getUserId(), category, amount)
            }
            .setNegativeButton("Cancel", null)
            .show()

        // Force dark background on dialog
        dialog.window?.setBackgroundDrawableResource(R.color.bg_card)
    }

    private fun observeSaveResult() {
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
            else Toast.makeText(requireContext(), "Failed to save budget", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Inline RecyclerView Adapter ──────────────────────────────────
    inner class BudgetAdapter(private val budgets: List<Budget>) :
        RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
            val b = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BudgetViewHolder(b)
        }
        override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) = holder.bind(budgets[position])
        override fun getItemCount() = budgets.size

        inner class BudgetViewHolder(private val b: ItemBudgetBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(budget: Budget) {
                b.tvCategory.text    = budget.category
                b.tvCategoryIcon.text = getCategoryEmoji(budget.category)
                b.tvSpentOf.text     = "₹${String.format("%.2f", budget.spentAmount)} of ₹${String.format("%.2f", budget.limitAmount)}"

                val percent = if (budget.limitAmount > 0)
                    ((budget.spentAmount / budget.limitAmount) * 100).toInt().coerceAtMost(100)
                else 0
                b.progressBudget.progress = percent

                val remaining = budget.limitAmount - budget.spentAmount
                b.tvRemaining.text = if (remaining >= 0)
                    "₹${String.format("%.2f", remaining)} remaining"
                else
                    "₹${String.format("%.2f", -remaining)} over budget!"

                when {
                    budget.spentAmount > budget.limitAmount -> {
                        b.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C62828"))
                        b.tvStatus.text = "Over Budget"
                        b.tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                        b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE"))
                        b.tvRemaining.setTextColor(android.graphics.Color.parseColor("#C62828"))
                    }
                    percent >= 80 -> {
                        b.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F57C00"))
                        b.tvStatus.text = "Near Limit"
                        b.tvStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"))
                        b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0"))
                        b.tvRemaining.setTextColor(android.graphics.Color.parseColor("#F57C00"))
                    }
                    else -> {
                        b.progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A73E8"))
                        b.tvStatus.text = "On Track"
                        b.tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                        b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9"))
                        b.tvRemaining.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                    }
                }

                b.root.setOnLongClickListener {
                    AlertDialog.Builder(b.root.context)
                        .setTitle("Delete Budget")
                        .setMessage("Remove the ${budget.category} budget?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteBudget(budget)
                            Toast.makeText(b.root.context, "Budget removed", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
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
        else -> "💳"
    }
}
