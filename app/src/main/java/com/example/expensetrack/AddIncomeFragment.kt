package com.example.expensetrack

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.expensetrack.Database.AppDatabase
import com.example.expensetrack.model.IncomeItem
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeFragment : Fragment() {

    private lateinit var btnIncomeDate: Button
    private lateinit var spinnerIncomeCategory: Spinner
    private lateinit var etIncomeAmount: EditText
    private lateinit var spinnerIncomeAccount: Spinner
    private lateinit var etIncomeNote: EditText
    private lateinit var btnSaveIncome: Button

    private lateinit var sharedViewModel: SharedViewModel

    private var selectedDateMillis: Long = 0L
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_income, container, false)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        initializeViews(view)
        setupDatePicker()
        setupSpinners()
        setupSaveButton()

        return view
    }


    private fun initializeViews(view: View) {
        btnIncomeDate = view.findViewById(R.id.btnIncomeDate)
        spinnerIncomeCategory = view.findViewById(R.id.spinnerIncomeCategory)
        etIncomeAmount = view.findViewById(R.id.etIncomeAmount)
        spinnerIncomeAccount = view.findViewById(R.id.spinnerIncomeAccount)
        etIncomeNote = view.findViewById(R.id.etIncomeNote)
        btnSaveIncome = view.findViewById(R.id.btnSaveIncome)

        val calendar = Calendar.getInstance()
        selectedDateMillis = calendar.timeInMillis
        btnIncomeDate.text = sdf.format(Date(selectedDateMillis))
    }

    private fun setupDatePicker() {
        btnIncomeDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    btnIncomeDate.text = sdf.format(Date(selectedDateMillis))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSpinners() {

        spinnerIncomeCategory.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.income_categories,  // You must create this array in res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerIncomeAccount.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.account_types,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupSaveButton() {
        btnSaveIncome.setOnClickListener {
            if (validateInputs()) {
                saveIncome()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val amountStr = etIncomeAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            showToast("Please enter an amount")
            return false
        }

        return try {
            amountStr.toDouble()
            true
        } catch (e: NumberFormatException) {
            showToast("Please enter a valid amount")
            false
        }
    }

    private fun saveIncome() {
        val category = spinnerIncomeCategory.selectedItem.toString()
        val amount = etIncomeAmount.text.toString().toDouble()
        val account = spinnerIncomeAccount.selectedItem.toString()
        val note = etIncomeNote.text.toString()
        val dateString = sdf.format(Date(selectedDateMillis))
        val color = getCategoryColor(category)

        val incomeItem = IncomeItem(
            amount = amount,
            account = account,
            date = dateString,
            note = note.ifEmpty { null },
            category = category,
            color = color
        )

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            db.incomeDao().insertIncome(incomeItem)


            AccountManager.addAmount(requireContext(), account, amount)



            requireActivity().runOnUiThread {
                showToast("Income added successfully")
                parentFragmentManager.popBackStack()
            }
        }

        val result = Bundle().apply {
            putDouble("income_amount", amount)
        }
        parentFragmentManager.setFragmentResult("income_update", result)
        parentFragmentManager.setFragmentResult("update_accounts", Bundle())
    }

    private fun getCategoryColor(category: String): Int {
        return when (category) {
            "Salary" -> Color.rgb(102, 187, 106)      // Green
            "Bonus" -> Color.rgb(255, 193, 7)         // Amber
            "Gift" -> Color.rgb(66, 165, 245)          // Blue
            "Interest" -> Color.rgb(255, 87, 34)       // Deep Orange
            "Other" -> Color.rgb(158, 158, 158)        // Grey
            else -> Color.LTGRAY
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
