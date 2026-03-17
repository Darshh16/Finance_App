package com.example.personalfinanceapp.ui.goals

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
import com.example.personalfinanceapp.R
import com.example.personalfinanceapp.data.model.SavingsGoal
import com.example.personalfinanceapp.databinding.DialogAddGoalBinding
import com.example.personalfinanceapp.databinding.FragmentGoalsBinding
import com.example.personalfinanceapp.databinding.ItemGoalBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoalsViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadGoals()

        binding.fabAddGoal.setOnClickListener { showAddGoalDialog() }
    }

    private fun setupRecyclerView() {
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadGoals() {
        val userId = sessionManager.getUserId()
        viewModel.getAllGoals(userId).observe(viewLifecycleOwner) { goals ->
            if (goals.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvGoals.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvGoals.visibility = View.VISIBLE
                binding.rvGoals.adapter = GoalsAdapter(goals)
            }
        }
    }

    private fun showAddGoalDialog() {
        val dialogBinding = DialogAddGoalBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext(), R.style.DarkDialog)
            .setTitle("New Savings Goal")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogBinding.etGoalTitle.text.toString().trim()
                val targetStr = dialogBinding.etTargetAmount.text.toString().trim()
                val savedStr = dialogBinding.etSavedAmount.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter a goal title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val target = targetStr.toDoubleOrNull()
                if (target == null || target <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid target amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val saved = savedStr.toDoubleOrNull() ?: 0.0

                val goal = SavingsGoal(
                    userId = sessionManager.getUserId(),
                    title = title,
                    targetAmount = target,
                    savedAmount = saved
                )
                viewModel.insertGoal(goal)
                Toast.makeText(requireContext(), "Goal created!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()

        dialog.window?.setBackgroundDrawableResource(R.color.bg_card)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class GoalsAdapter(private val goals: List<SavingsGoal>) :
        RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
            val b = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return GoalViewHolder(b)
        }

        override fun onBindViewHolder(holder: GoalViewHolder, position: Int) =
            holder.bind(goals[position])

        override fun getItemCount() = goals.size

        inner class GoalViewHolder(private val b: ItemGoalBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(goal: SavingsGoal) {
                b.tvGoalTitle.text = goal.title
                b.tvGoalSaved.text = "₹${String.format("%.2f", goal.savedAmount)} saved"
                b.tvGoalTarget.text = "of ₹${String.format("%.2f", goal.targetAmount)}"

                val percent = if (goal.targetAmount > 0)
                    ((goal.savedAmount / goal.targetAmount) * 100).toInt().coerceAtMost(100)
                else 0
                b.progressGoal.progress = percent

                if (goal.deadline != null) {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    b.tvGoalDeadline.text = "Due: ${sdf.format(Date(goal.deadline))}"
                } else {
                    b.tvGoalDeadline.text = "$percent% complete"
                }

                if (goal.isCompleted || percent >= 100) {
                    b.tvGoalStatus.text = "✓ Done"
                    b.tvGoalStatus.setTextColor(
                        android.graphics.Color.parseColor("#00E676")
                    )
                    b.progressGoal.progressTintList =
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#00E676")
                        )
                } else {
                    b.tvGoalStatus.text = "Active"
                    b.tvGoalStatus.setTextColor(
                        android.graphics.Color.parseColor("#FFD700")
                    )
                }

                // Long press to delete
                b.root.setOnLongClickListener {
                    AlertDialog.Builder(b.root.context, R.style.DarkDialog)
                        .setTitle("Delete Goal")
                        .setMessage("Remove '${goal.title}'?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteGoal(goal)
                            Toast.makeText(b.root.context, "Goal deleted", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                        .also { it.window?.setBackgroundDrawableResource(R.color.bg_card) }
                    true
                }
            }
        }
    }
}