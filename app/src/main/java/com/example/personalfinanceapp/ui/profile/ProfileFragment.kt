package com.example.personalfinanceapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.personalfinanceapp.databinding.FragmentProfileBinding
import com.example.personalfinanceapp.ui.auth.LoginActivity
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.BudgetViewModel
import com.example.personalfinanceapp.viewmodel.DashboardViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupProfileInfo()
        setupDarkModeSwitch()
        setupLogout()
        loadStats()
    }

    private fun setupProfileInfo() {
        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email.ifEmpty { "No email saved" }
        val initials = if (name.isNotEmpty()) name[0].uppercaseChar().toString() else "U"
        binding.tvAvatar.text = initials
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener(null)
        binding.switchDarkMode.isChecked = sessionManager.isDarkMode()

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            // No recreate needed — configChanges handles it silently
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    sessionManager.clearSession()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadStats() {
        val userId = sessionManager.getUserId()
        dashboardViewModel.loadData(userId)
        dashboardViewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            binding.tvStatTransactions.text = transactions.size.toString()
        }
        budgetViewModel.getBudgetsForCurrentMonth(userId).observe(viewLifecycleOwner) { budgets ->
            binding.tvStatBudgets.text = budgets.size.toString()
        }
        binding.tvStatGoals.text = "0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}