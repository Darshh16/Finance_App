package com.example.personalfinanceapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinanceapp.databinding.FragmentDashboardBinding
import com.example.personalfinanceapp.ui.transaction.TransactionAdapter
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.DashboardViewModel
import com.example.personalfinanceapp.viewmodel.TransactionViewModel
import java.util.Calendar
import com.example.personalfinanceapp.utils.InsightsHelper

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        setupGreeting()
        setupSeeAll()
        loadData()
    }

    private fun showInsights(transactions: List<com.example.personalfinanceapp.data.model.Transaction>) {
        val insights = InsightsHelper.generateInsights(transactions)
        val insightViews = listOf(binding.tvInsight1, binding.tvInsight2, binding.tvInsight3)
        insightViews.forEachIndexed { index, textView ->
            if (index < insights.size) {
                textView.text = insights[index]
                textView.visibility = android.view.View.VISIBLE
            } else {
                textView.visibility = android.view.View.GONE
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                Toast.makeText(requireContext(), transaction.category, Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { transaction ->
                transactionViewModel.deleteTransaction(transaction)
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning!"
            hour < 17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        val name = sessionManager.getUserName()
        binding.tvUserName.text = name
        // Set avatar initial
        binding.tvAvatarDash.text = if (name.isNotEmpty()) name[0].uppercaseChar().toString() else "U"
    }

    private fun setupSeeAll() {
        binding.tvSeeAll.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.example.personalfinanceapp.R.id.bottom_navigation
            ).selectedItemId = com.example.personalfinanceapp.R.id.navigation_transactions
        }
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()
        viewModel.loadData(userId)
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "₹${String.format("%.2f", income)}"
            updateBalance()
        }
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "₹${String.format("%.2f", expense)}"
            updateBalance()
        }
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvTransactions.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE
                adapter.submitList(transactions.take(10))
                showInsights(transactions)
            }
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        val balance = viewModel.getBalance(income, expense)
        binding.tvBalance.text = "₹${String.format("%.2f", balance)}"
        binding.tvBalance.setTextColor(
            if (balance >= 0) android.graphics.Color.parseColor("#FFD700")
            else android.graphics.Color.parseColor("#FF5252")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
