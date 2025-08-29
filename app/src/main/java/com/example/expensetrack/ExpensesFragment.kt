package com.example.expensetrack

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetrack.Database.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpensesFragment : Fragment() {

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
        "Food" to Color.rgb(255, 153, 153),
        "Transport" to Color.rgb(153, 204, 255),
        "Education" to Color.rgb(255, 204, 153),
        "Cloths" to Color.rgb(204, 153, 255),
        "Health" to Color.rgb(153, 255, 204),
        "Pets" to Color.rgb(255, 255, 153),
        "Beauty" to Color.rgb(255, 153, 255),
        "Household" to Color.rgb(204, 204, 204)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)

        pieChart = view.findViewById(R.id.expensesPieChart)
        detailContainer = view.findViewById(R.id.expensesDetailsContainer)
        btnToggleView = view.findViewById(R.id.btnToggleView)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        tvCurrentRange = view.findViewById(R.id.tvCurrentRange)

        btnToggleView.text = viewMode
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

        parentFragmentManager.setFragmentResultListener("expenses_request", this) { _, _ ->
            loadChartData()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadChartData()
    }

    public fun loadChartData() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val allExpenses = db.expenseDao().getAllExpenses()

            val now = Calendar.getInstance()
            val adjusted = now.clone() as Calendar
            when (viewMode) {
                "Month" -> adjusted.add(Calendar.MONTH, currentIndex)
                "Week" -> adjusted.add(Calendar.WEEK_OF_YEAR, currentIndex)
                "Day" -> adjusted.add(Calendar.DAY_OF_YEAR, currentIndex)
            }

            val filtered = allExpenses.filter {
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

            val dataSet = PieDataSet(entries, "Expenses").apply {
                setColors(colors)
                valueTextSize = 14f
            }

            pieChart.data = PieData(dataSet)
            pieChart.centerText = "Total: Rs.${filtered.sumOf { it.amount }}"
            pieChart.setEntryLabelColor(Color.BLACK)
            pieChart.description.isEnabled = false
            pieChart.invalidate()

            val rangeText = when (viewMode) {
                "Month" -> "Month: ${adjusted.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${adjusted.get(Calendar.YEAR)}"
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
                detailView.findViewById<TextView>(R.id.tvNote).text = item.note ?: ""
                detailContainer.addView(detailView)
            }
        }
    }
}
