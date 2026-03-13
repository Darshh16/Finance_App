package com.example.personalfinanceapp.ui.dashboard

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
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
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
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transaction ->
            Toast.makeText(requireContext(), "Tapped: ${transaction.category}", Toast.LENGTH_SHORT).show()
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning!"
            hour < 17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        binding.tvGreeting.text = greeting
        binding.tvUserName.text = sessionManager.getUserName()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()
        viewModel.loadData(userId)

        // Observe income
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "₹${ String.format("%.2f", income) }"
            updateBalance()
        }

        // Observe expense
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvExpense.text = "₹${ String.format("%.2f", expense) }"
            updateBalance()
        }

        // Observe transactions list
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvTransactions.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE
                adapter.submitList(transactions.take(10)) // show last 10
            }
        }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        val balance = viewModel.getBalance(income, expense)
        binding.tvBalance.text = "₹${ String.format("%.2f", balance) }"
        binding.tvBalance.setTextColor(
            if (balance >= 0) android.graphics.Color.parseColor("#1A1A1A")
            else android.graphics.Color.parseColor("#C62828")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
