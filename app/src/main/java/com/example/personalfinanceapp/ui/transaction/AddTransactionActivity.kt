package com.example.personalfinanceapp.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinanceapp.data.model.Transaction
import com.example.personalfinanceapp.data.model.TransactionCategory
import com.example.personalfinanceapp.databinding.ActivityAddTransactionBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: TransactionViewModel by viewModels()

    private var selectedType = "EXPENSE"
    private var selectedDateMillis = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)



        // Set default state — EXPENSE selected
        setExpenseSelected()

        binding.btnExpense.setOnClickListener {
            selectedType = "EXPENSE"
            setExpenseSelected()
            updateCategorySpinner()
        }

        binding.btnIncome.setOnClickListener {
            selectedType = "INCOME"
            setIncomeSelected()
            updateCategorySpinner()
        }

        updateCategorySpinner()
        setupDatePicker()
        setupSaveButton()
        observeViewModel()

        binding.etDate.setText(dateFormat.format(selectedDateMillis))
    }

    // Red active button for EXPENSE
    private fun setExpenseSelected() {
        binding.btnExpense.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#C62828")
            )
        binding.btnIncome.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#CCCCCC")
            )
    }

    // Green active button for INCOME
    private fun setIncomeSelected() {
        binding.btnIncome.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2E7D32")
            )
        binding.btnExpense.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#CCCCCC")
            )
    }

    private fun updateCategorySpinner() {
        val categories = if (selectedType == "EXPENSE")
            TransactionCategory.expenseCategories
        else
            TransactionCategory.incomeCategories

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedDateMillis = cal.timeInMillis
                    binding.etDate.setText(dateFormat.format(selectedDateMillis))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: ""

            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Please enter an amount"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }
            binding.tilAmount.error = null

            val transaction = Transaction(
                userId = sessionManager.getUserId(),
                type = selectedType,
                amount = amount,
                category = category,
                description = description,
                date = selectedDateMillis
            )

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            viewModel.saveTransaction(transaction)
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(this) { success ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true
            if (success) {
                val msg = if (selectedType == "INCOME") "Income added! ✓" else "Expense added! ✓"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}