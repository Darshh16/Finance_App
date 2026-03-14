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

    // Keep full list so we can filter locally
    private var allTransactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                Toast.makeText(
                    requireContext(),
                    "${transaction.category}: ₹${String.format("%.2f", transaction.amount)}",
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
        binding.chipAll.setOnClickListener { applyFilter("ALL") }
        binding.chipIncome.setOnClickListener { applyFilter("INCOME") }
        binding.chipExpense.setOnClickListener { applyFilter("EXPENSE") }
    }

    private fun loadTransactions() {
        val userId = sessionManager.getUserId()
        viewModel.getAllTransactions(userId).observe(viewLifecycleOwner) { transactions ->
            allTransactions = transactions
            applyFilter(getSelectedFilter())
        }
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "INCOME" -> allTransactions.filter { it.type == "INCOME" }
            "EXPENSE" -> allTransactions.filter { it.type == "EXPENSE" }
            else -> allTransactions
        }

        adapter.submitList(filtered)
        binding.tvCount.text = "${filtered.size} transaction${if (filtered.size != 1) "s" else ""}"

        if (filtered.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvTransactions.visibility = View.VISIBLE
        }
    }

    private fun getSelectedFilter(): String {
        return when {
            binding.chipIncome.isChecked -> "INCOME"
            binding.chipExpense.isChecked -> "EXPENSE"
            else -> "ALL"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
