package com.example.expensetrack

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetrack.Database.AppDatabase
import com.example.expensetrack.model.IncomeItem
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class IncomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var detailContainer: LinearLayout
    private lateinit var btnToggleView: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvCurrentRange: TextView

    private var viewMode = "Month"
    private var currentIndex = 0

    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    private val categoryColors = mapOf(
        "Salary" to Color.rgb(144, 238, 144),
        "Business" to Color.rgb(173, 216, 230),
        "Allowance" to Color.rgb(255, 255, 153),
        "Bonus" to Color.rgb(255, 182, 193),
        "Other" to Color.rgb(224, 224, 224)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_income, container, false)

        pieChart = view.findViewById(R.id.incomePieChart)
        detailContainer = view.findViewById(R.id.incomeDetailsContainer)
        btnToggleView = view.findViewById(R.id.btnToggleView)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        tvCurrentRange = view.findViewById(R.id.tvCurrentRange)

        btnToggleView.setOnClickListener {
            viewMode = when (viewMode) {
                "Month" -> "Week"
                "Week" -> "Day"
                else -> "Month"
            }
            btnToggleView.text = viewMode
            currentIndex = 0
            loadChartData()
        }

        btnPrev.setOnClickListener {
            currentIndex--
            loadChartData()
        }

        btnNext.setOnClickListener {
            currentIndex++
            loadChartData()
        }

        loadChartData()

        parentFragmentManager.setFragmentResultListener("income_request", this) { _, _ ->
            loadChartData()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadChartData()
    }

    public fun loadChartData() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            try {
                val incomeList = db.incomeDao().getAllIncomes()
                displayChartData(incomeList)
            } catch (e: Exception) {
                Log.e("IncomeFragment", "Error loading income data", e)
                Toast.makeText(requireContext(), "Error loading income data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayChartData(incomeList: List<IncomeItem>) {
        val now = Calendar.getInstance()
        val adjusted = now.clone() as Calendar
        when (viewMode) {
            "Month" -> adjusted.add(Calendar.MONTH, currentIndex)
            "Week" -> adjusted.add(Calendar.WEEK_OF_YEAR, currentIndex)
            "Day" -> adjusted.add(Calendar.DAY_OF_YEAR, currentIndex)
        }

        val filtered = incomeList.filter {
            try {
                val date = sdf.parse(it.date)
                val cal = Calendar.getInstance().apply { time = date }

                when (viewMode) {
                    "Month" -> cal.get(Calendar.MONTH) == adjusted.get(Calendar.MONTH) &&
                            cal.get(Calendar.YEAR) == adjusted.get(Calendar.YEAR)

                    "Week" -> cal.get(Calendar.WEEK_OF_YEAR) == adjusted.get(Calendar.WEEK_OF_YEAR) &&
                            cal.get(Calendar.YEAR) == adjusted.get(Calendar.YEAR)

                    "Day" -> cal.get(Calendar.DAY_OF_YEAR) == adjusted.get(Calendar.DAY_OF_YEAR) &&
                            cal.get(Calendar.YEAR) == adjusted.get(Calendar.YEAR)

                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }

        val categoryMap = mutableMapOf<String, Double>()
        for (item in filtered) {
            categoryMap[item.category] = (categoryMap[item.category] ?: 0.0) + item.amount
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        for ((category, amount) in categoryMap) {
            entries.add(PieEntry(amount.toFloat(), category))
            colors.add(categoryColors[category] ?: Color.LTGRAY)
        }

        val dataSet = PieDataSet(entries, "Income").apply {
            setColors(colors)
            valueTextSize = 14f
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.centerText = "Total: Rs.${filtered.sumOf { it.amount }}"
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.description.isEnabled = false
        pieChart.invalidate()

        val rangeText = when (viewMode) {
            "Month" -> "Month: ${
                adjusted.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            } ${adjusted.get(Calendar.YEAR)}"
            "Week" -> "Week: ${adjusted.get(Calendar.WEEK_OF_YEAR)}, ${adjusted.get(Calendar.YEAR)}"
            "Day" -> "Day: ${sdf.format(adjusted.time)}"
            else -> ""
        }
        tvCurrentRange.text = rangeText

        detailContainer.removeAllViews()
        val total = filtered.sumOf { it.amount }
        for (item in filtered) {
            val percentage = if (total != 0.0) (item.amount / total * 100).toInt() else 0
            val detailView = layoutInflater.inflate(R.layout.item_expense_detail, detailContainer, false)
            detailView.findViewById<TextView>(R.id.tvCategoryName).text = item.category
            detailView.findViewById<TextView>(R.id.tvAmount).text = "Rs.${item.amount}"
            detailView.findViewById<TextView>(R.id.tvPercentage).text = "$percentage%"
            detailView.findViewById<TextView>(R.id.tvAccount).text = item.account
            detailView.findViewById<TextView>(R.id.tvDate).text = item.date
            detailContainer.addView(detailView)
        }
    }
}
