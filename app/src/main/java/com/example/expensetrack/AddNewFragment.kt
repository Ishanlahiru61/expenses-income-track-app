package com.example.expensetrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class AddNewFragment : Fragment() {

    private lateinit var btnToggle: Button
    private lateinit var btnTransfer: Button
    private var showingIncome = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_new, container, false)

        btnToggle = view.findViewById(R.id.btnToggle)
        btnTransfer = view.findViewById(R.id.btnTransfer)

        updateToggleButtonStyle()
        setAccentColor(btnTransfer)

        btnToggle.setOnClickListener {
            showingIncome = !showingIncome
            updateToggleButtonStyle()
            if (showingIncome) {
                loadChildFragment(AddIncomeFragment(), "right")
            } else {
                loadChildFragment(AddExpenseFragment(), "left")
            }
        }

        btnTransfer.setOnClickListener {
            loadChildFragment(TransferFragment(), "bottom")

            parentFragmentManager.setFragmentResult("update_accounts", Bundle())
        }


        loadChildFragment(AddIncomeFragment(), null)

        return view
    }

    private fun updateToggleButtonStyle() {
        if (showingIncome) {
            btnToggle.text = "Expenses"
            setOrangeColor(btnToggle)
        } else {
            btnToggle.text = "Income"
            setAccentColor(btnToggle)
        }
    }

    private fun setAccentColor(button: Button) {
        val color = ContextCompat.getColor(requireContext(), R.color.Accent_color)
        button.setBackgroundColor(color)
        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun setOrangeColor(button: Button) {
        val color = ContextCompat.getColor(requireContext(), R.color.orange_color)
        button.setBackgroundColor(color)
        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun loadChildFragment(fragment: Fragment, direction: String?) {
        val transaction = childFragmentManager.beginTransaction()

        when (direction) {
            "left" -> transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            "right" -> transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            "bottom" -> transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out)
        }

        transaction.replace(R.id.nestedFragmentContainer, fragment).commit()
    }
}
