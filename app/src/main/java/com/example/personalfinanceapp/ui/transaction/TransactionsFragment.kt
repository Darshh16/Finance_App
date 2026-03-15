package com.example.personalfinanceapp.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinanceapp.data.model.Transaction
import com.example.personalfinanceapp.databinding.FragmentTransactionsBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.TransactionViewModel

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TransactionAdapter

    // Hold full list in memory so chip filter works instantly
    private var allTransactions: List<Transaction> = emptyList()
    private var currentFilter = "ALL"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        setupFilterChips()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                val sign = if (transaction.type == "INCOME") "+" else "-"
                Toast.makeText(
                    requireContext(),
                    "${transaction.category}: $sign₹${String.format("%.2f", transaction.amount)}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { transaction ->
                viewModel.deleteTransaction(transaction)
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) { currentFilter = "ALL"; applyFilter() }
        }
        binding.chipIncome.setOnCheckedChangeListener { _, checked ->
            if (checked) { currentFilter = "INCOME"; applyFilter() }
        }
        binding.chipExpense.setOnCheckedChangeListener { _, checked ->
            if (checked) { currentFilter = "EXPENSE"; applyFilter() }
        }
    }

    private fun loadTransactions() {
        val userId = sessionManager.getUserId()
        viewModel.getAllTransactions(userId).observe(viewLifecycleOwner) { transactions ->
            allTransactions = transactions
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "INCOME"  -> allTransactions.filter { it.type == "INCOME" }
            "EXPENSE" -> allTransactions.filter { it.type == "EXPENSE" }
            else      -> allTransactions
        }

        adapter.submitList(filtered.toMutableList())

        val label = when (currentFilter) {
            "INCOME"  -> "${filtered.size} income transaction${if (filtered.size != 1) "s" else ""}"
            "EXPENSE" -> "${filtered.size} expense transaction${if (filtered.size != 1) "s" else ""}"
            else      -> "${filtered.size} transaction${if (filtered.size != 1) "s" else ""}"
        }
        binding.tvCount.text = label

        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvTransactions.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
