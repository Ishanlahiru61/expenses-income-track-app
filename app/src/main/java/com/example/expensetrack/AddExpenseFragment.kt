package com.example.expensetrack

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.expensetrack.model.ExpenseItem
import com.example.expensetrack.Database.AppDatabase

import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var btnExpenseDate: Button
    private lateinit var spinnerExpenseCategory: Spinner
    private lateinit var etExpenseAmount: EditText
    private lateinit var spinnerExpenseAccount: Spinner
    private lateinit var etExpenseNote: EditText
    private lateinit var btnSaveExpense: Button

    private lateinit var sharedViewModel: SharedViewModel

    private var selectedDateMillis: Long = 0L
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        initializeViews(view)
        setupDatePicker()
        setupSpinners()
        setupSaveButton()
        return view
    }

    private fun initializeViews(view: View) {
        btnExpenseDate = view.findViewById(R.id.btnExpenseDate)
        spinnerExpenseCategory = view.findViewById(R.id.spinnerExpenseCategory)
        etExpenseAmount = view.findViewById(R.id.etExpenseAmount)
        spinnerExpenseAccount = view.findViewById(R.id.spinnerExpenseAccount)
        etExpenseNote = view.findViewById(R.id.etExpenseNote)
        btnSaveExpense = view.findViewById(R.id.btnSaveExpense)

        val calendar = Calendar.getInstance()
        selectedDateMillis = calendar.timeInMillis
        btnExpenseDate.text = sdf.format(Date(selectedDateMillis))
    }

    private fun setupDatePicker() {
        btnExpenseDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    btnExpenseDate.text = sdf.format(Date(selectedDateMillis))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSpinners() {
        spinnerExpenseCategory.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.expense_categories,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerExpenseAccount.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.account_types,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupSaveButton() {
        btnSaveExpense.setOnClickListener {
            if (validateInputs()) {
                saveExpense()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val amountStr = etExpenseAmount.text.toString().trim()
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

    private fun saveExpense() {
        val category = spinnerExpenseCategory.selectedItem.toString()
        val amount = etExpenseAmount.text.toString().toDouble()
        val account = spinnerExpenseAccount.selectedItem.toString()
        val note = etExpenseNote.text.toString()
        val dateString = sdf.format(Date(selectedDateMillis))

        val expenseItem = ExpenseItem(
            category = category,
            amount = amount,
            account = account,
            date = dateString,
            color = getCategoryColor(category),
            note = note.ifEmpty { null }
        )

        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.expenseDao().insertExpense(expenseItem)

            AccountManager.deductAmount(requireContext(), account, amount)


            sharedViewModel.sendExpense(amount)

            requireActivity().runOnUiThread {
                showToast("Expense added successfully")
                parentFragmentManager.popBackStack()
            }
        }

        val result = Bundle().apply {
            putDouble("expense_amount", amount)
        }
        parentFragmentManager.setFragmentResult("expense_update", result)
        parentFragmentManager.setFragmentResult("update_accounts", Bundle())
    }

    private fun getCategoryColor(category: String): Int {
        return when (category) {
            "Food" -> Color.rgb(255, 153, 153)
            "Transport" -> Color.rgb(153, 204, 255)
            "Education" -> Color.rgb(255, 204, 153)
            "Cloths" -> Color.rgb(204, 153, 255)
            "Health" -> Color.rgb(153, 255, 204)
            "Pets" -> Color.rgb(255, 255, 153)
            "Beauty" -> Color.rgb(255, 153, 255)
            "Household" -> Color.rgb(204, 204, 204)
            else -> Color.LTGRAY
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
