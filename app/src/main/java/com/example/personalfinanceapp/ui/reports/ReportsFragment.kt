package com.example.personalfinanceapp.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinanceapp.databinding.FragmentReportsBinding
import com.example.personalfinanceapp.databinding.ItemCategoryStatBinding
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.viewmodel.CategoryStat
import com.example.personalfinanceapp.viewmodel.ReportsViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    // Chart colors — one per category
    private val chartColors = listOf(
        Color.parseColor("#1A73E8"),
        Color.parseColor("#E53935"),
        Color.parseColor("#43A047"),
        Color.parseColor("#FB8C00"),
        Color.parseColor("#8E24AA"),
        Color.parseColor("#00ACC1"),
        Color.parseColor("#F4511E"),
        Color.parseColor("#3949AB"),
        Color.parseColor("#00897B"),
        Color.parseColor("#FFB300")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())

        setupPieChart()
        setupBarChart()
        loadData()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()
        viewModel.loadReports(userId)

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTotalIncome.text = "₹${String.format("%.2f", income)}"
            updateBarChart(income, viewModel.totalExpense.value ?: 0.0)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvTotalExpense.text = "₹${String.format("%.2f", expense)}"
            updateBarChart(viewModel.totalIncome.value ?: 0.0, expense)
        }

        viewModel.categoryStats.observe(viewLifecycleOwner) { stats ->
            if (stats.isEmpty()) {
                binding.tvNoData.visibility = View.VISIBLE
                binding.rvCategories.visibility = View.GONE
                clearPieChart()
            } else {
                binding.tvNoData.visibility = View.GONE
                binding.rvCategories.visibility = View.VISIBLE
                updatePieChart(stats)
                binding.rvCategories.adapter = CategoryAdapter(stats)
            }
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 52f
            transparentCircleRadius = 57f
            setHoleColor(Color.WHITE)
            setUsePercentValues(true)
            setEntryLabelTextSize(11f)
            setEntryLabelColor(Color.WHITE)
            legend.apply {
                isEnabled = true
                orientation = Legend.LegendOrientation.VERTICAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                textSize = 11f
            }
            setNoDataText("Add expense transactions to see the chart")
        }
    }

    private fun updatePieChart(stats: List<CategoryStat>) {
        val entries = stats.mapIndexed { _, stat ->
            PieEntry(stat.percent, stat.category)
        }
        val dataSet = PieDataSet(entries, "").apply {
            colors = chartColors.take(entries.size)
            sliceSpace = 2f
            selectionShift = 6f
            valueTextSize = 10f
            valueTextColor = Color.WHITE
        }
        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun clearPieChart() {
        binding.pieChart.clear()
        binding.pieChart.invalidate()
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setFitBars(true)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(listOf("Income", "Expenses"))
                granularity = 1f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
            }
            legend.isEnabled = false
            setNoDataText("No transaction data yet")
        }
    }

    private fun updateBarChart(income: Double, expense: Double) {
        val entries = listOf(
            BarEntry(0f, income.toFloat()),
            BarEntry(1f, expense.toFloat())
        )
        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#43A047"),  // green for income
                Color.parseColor("#E53935")   // red for expense
            )
            valueTextSize = 11f
            valueTextColor = Color.DKGRAY
        }
        binding.barChart.data = BarData(dataSet).apply { barWidth = 0.5f }
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Category stats list adapter ──────────────────────────────────
    inner class CategoryAdapter(private val stats: List<CategoryStat>) :
        RecyclerView.Adapter<CategoryAdapter.StatViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
            val b = ItemCategoryStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return StatViewHolder(b)
        }
        override fun onBindViewHolder(holder: StatViewHolder, position: Int) = holder.bind(stats[position], position)
        override fun getItemCount() = stats.size

        inner class StatViewHolder(private val b: ItemCategoryStatBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(stat: CategoryStat, index: Int) {
                b.tvCategoryName.text = stat.category
                b.tvAmount.text = "-₹${String.format("%.2f", stat.amount)}"
                b.tvPercent.text = "${String.format("%.1f", stat.percent)}%"
                b.progressCategory.progress = stat.percent.toInt()
                val color = chartColors[index % chartColors.size]
                b.viewDot.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
                b.progressCategory.progressTintList = android.content.res.ColorStateList.valueOf(color)
            }
        }
    }
}
