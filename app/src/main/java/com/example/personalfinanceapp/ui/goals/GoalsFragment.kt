package com.example.personalfinanceapp.ui.goals

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.data.model.SavingsGoal
import com.example.personalfinanceapp.databinding.DialogAddGoalBinding
import com.example.personalfinanceapp.databinding.DialogAddSavingsBinding
import com.example.personalfinanceapp.databinding.FragmentGoalsBinding
import com.example.personalfinanceapp.databinding.ItemGoalBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoalsViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var allGoals: List<SavingsGoal> = emptyList()
    private var showActiveOnly = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddGoal.setOnClickListener { showAddGoalDialog() }

        setupChips()
        loadGoals()
        observeSaveResult()
    }

    private fun setupChips() {
        binding.chipActive.setOnCheckedChangeListener { _, checked ->
            if (checked) { showActiveOnly = true; applyFilter() }
        }
        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) { showActiveOnly = false; applyFilter() }
        }
    }

    private fun loadGoals() {
        val userId = sessionManager.getUserId()
        viewModel.getAllGoals(userId).observe(viewLifecycleOwner) { goals ->
            allGoals = goals
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = if (showActiveOnly)
            allGoals.filter { !it.isCompleted }
        else
            allGoals

        val activeCount = allGoals.count { !it.isCompleted }
        binding.tvGoalSummary.text = "$activeCount active goal${if (activeCount != 1) "s" else ""}"

        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvGoals.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvGoals.visibility = View.VISIBLE
            binding.rvGoals.adapter = GoalAdapter(filtered)
        }
    }

    private fun showAddGoalDialog() {
        val dialogBinding = DialogAddGoalBinding.inflate(layoutInflater)
        var selectedDeadline: Long? = null

        // Deadline date picker
        dialogBinding.etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedDeadline = cal.timeInMillis
                dialogBinding.etDeadline.setText(dateFormat.format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Create Savings Goal")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                val title = dialogBinding.etGoalTitle.text.toString().trim()
                val amountStr = dialogBinding.etTargetAmount.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a goal title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Please enter a valid target amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.createGoal(sessionManager.getUserId(), title, amount, selectedDeadline)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddSavingsDialog(goal: SavingsGoal) {
        val dialogBinding = DialogAddSavingsBinding.inflate(layoutInflater)
        val remaining = goal.targetAmount - goal.savedAmount
        dialogBinding.tvGoalInfo.text =
            "Goal: ${goal.title}\nSaved: ₹${String.format("%.2f", goal.savedAmount)} / ₹${String.format("%.2f", goal.targetAmount)}\nStill needed: ₹${String.format("%.2f", remaining)}"

        AlertDialog.Builder(requireContext())
            .setTitle("Add Savings")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = dialogBinding.etSavingsAmount.text.toString().trim()
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addSavings(goal, amount)
                Toast.makeText(requireContext(), "₹${String.format("%.2f", amount)} added to ${goal.title}!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeSaveResult() {
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) Toast.makeText(requireContext(), "Goal created!", Toast.LENGTH_SHORT).show()
            else Toast.makeText(requireContext(), "Failed to create goal", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Inline RecyclerView Adapter ──────────────────────────────────
    inner class GoalAdapter(private val goals: List<SavingsGoal>) :
        RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
            val b = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return GoalViewHolder(b)
        }
        override fun onBindViewHolder(holder: GoalViewHolder, position: Int) = holder.bind(goals[position])
        override fun getItemCount() = goals.size

        inner class GoalViewHolder(private val b: ItemGoalBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(goal: SavingsGoal) {
                b.tvGoalTitle.text = goal.title
                b.tvGoalEmoji.text = getGoalEmoji(goal.title)
                b.tvSavedAmount.text = "₹${String.format("%.2f", goal.savedAmount)}"
                b.tvTargetAmount.text = "of ₹${String.format("%.2f", goal.targetAmount)}"

                // Deadline
                b.tvDeadline.text = if (goal.deadline != null)
                    "Target: ${dateFormat.format(java.util.Date(goal.deadline))}"
                else "No deadline set"

                // Progress
                val percent = if (goal.targetAmount > 0)
                    ((goal.savedAmount / goal.targetAmount) * 100).toInt().coerceAtMost(100)
                else 0
                b.progressGoal.progress = percent
                b.tvPercent.text = "$percent% saved"

                val remaining = goal.targetAmount - goal.savedAmount
                b.tvRemaining.text = if (remaining > 0)
                    "₹${String.format("%.2f", remaining)} to go"
                else "Goal reached! 🎉"

                // Completed state
                if (goal.isCompleted) {
                    b.tvCompletedBadge.visibility = View.VISIBLE
                    b.btnAddSavings.isEnabled = false
                    b.btnAddSavings.text = "Completed ✓"
                    b.progressGoal.progressTintList =
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#2E7D32")
                        )
                } else {
                    b.tvCompletedBadge.visibility = View.GONE
                    b.btnAddSavings.isEnabled = true
                    b.btnAddSavings.text = "+ Add Savings"
                    b.progressGoal.progressTintList =
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#1A73E8")
                        )
                }

                b.btnAddSavings.setOnClickListener { showAddSavingsDialog(goal) }

                // Long press to delete
                b.root.setOnLongClickListener {
                    AlertDialog.Builder(b.root.context)
                        .setTitle("Delete Goal")
                        .setMessage("Delete the goal '${goal.title}'?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteGoal(goal)
                            Toast.makeText(b.root.context, "Goal deleted", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }
        }
    }

    private fun getGoalEmoji(title: String): String {
        val t = title.lowercase()
        return when {
            t.contains("laptop") || t.contains("computer") || t.contains("phone") -> "💻"
            t.contains("car") || t.contains("bike") || t.contains("vehicle") -> "🚗"
            t.contains("vacation") || t.contains("trip") || t.contains("travel") -> "✈️"
            t.contains("house") || t.contains("home") || t.contains("flat") -> "🏠"
            t.contains("wedding") || t.contains("marriage") -> "💍"
            t.contains("education") || t.contains("study") || t.contains("college") -> "📚"
            t.contains("emergency") || t.contains("fund") -> "🏦"
            t.contains("gift") -> "🎁"
            else -> "🎯"
        }
    }
}
