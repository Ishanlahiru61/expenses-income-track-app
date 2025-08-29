package com.example.expensetrack

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetrack.Database.AppDatabase
import com.example.expensetrack.model.TransferItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class TransferFragment : Fragment() {

    private lateinit var dateBtn: Button
    private lateinit var amountEditText: EditText
    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button

    private val accountList = listOf("Bank Account", "Card", "Petty Cash")
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private var selectedDate = sdf.format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_transfer, container, false).also { view ->
        dateBtn       = view.findViewById(R.id.btnTransferDate)
        amountEditText= view.findViewById(R.id.etTransferAmount)
        fromSpinner   = view.findViewById(R.id.spinnerTransferFrom)
        toSpinner     = view.findViewById(R.id.spinnerTransferTo)
        noteEditText  = view.findViewById(R.id.etTransferNote)
        saveButton    = view.findViewById(R.id.btnSaveTransfer)


        dateBtn.text = selectedDate
        dateBtn.setOnClickListener { openDatePicker() }


        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, accountList
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        fromSpinner.adapter = adapter
        toSpinner.adapter   = adapter

        saveButton.setOnClickListener { saveTransfer() }
    }

    private fun openDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, y, m, d ->
                c.set(y, m, d)
                selectedDate = sdf.format(c.time)
                dateBtn.text = selectedDate
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTransfer() {
        val amountText = amountEditText.text.toString()
        if (amountText.isBlank()) {
            showToast("Enter amount"); return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showToast("Invalid amount"); return
        }

        val fromAcc = fromSpinner.selectedItem.toString()
        val toAcc   = toSpinner.selectedItem.toString()
        if (fromAcc == toAcc) {
            showToast("Cannot transfer to the same account!"); return
        }

        val item = TransferItem(
            date = selectedDate,
            amount = amount,
            fromAccount = fromAcc,
            toAccount = toAcc,
            note = noteEditText.text.toString().ifEmpty { null }
        )


        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.transferDao().insertTransfer(item)


            AccountManager.deductAmount(requireContext(), fromAcc, amount)
            AccountManager.addAmount(requireContext(), toAcc, amount)


            requireActivity().runOnUiThread {
                parentFragmentManager.setFragmentResult("update_accounts", Bundle())
                showToast("Transfer saved!")
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun showToast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
