package com.example.expensetrack

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class StatsFragment : Fragment() {

    private lateinit var toggleButton: MaterialButton
    private var showingIncome = true
    private var currentFragment: Fragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        Log.d("StatsFragment", "onCreateView() called")

        toggleButton = view.findViewById(R.id.btnToggle)
        updateButtonUI()

        toggleButton.setOnClickListener {
            showingIncome = !showingIncome
            val fragment = if (showingIncome) IncomeFragment() else ExpensesFragment()
            val direction = if (showingIncome) "right" else "left"
            loadFragment(fragment, direction)
            updateButtonUI()
        }

        // Load default fragment
        if (savedInstanceState == null) {
            Log.d("StatsFragment", "Loading initial IncomeFragment")
            loadFragment(IncomeFragment(), null)
        } else {
            Log.d("StatsFragment", "Restoring state")
            showingIncome = savedInstanceState.getBoolean("showingIncome", true)
            updateButtonUI()
            currentFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
            Log.d("StatsFragment", "Restored currentFragment: $currentFragment")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("StatsFragment", "onViewCreated() called")
        if (currentFragment == null) {
            currentFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
            Log.d("StatsFragment", "Found currentFragment in onViewCreated: $currentFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("StatsFragment", "onResume() called")
        Log.d("StatsFragment", "currentFragment: $currentFragment")
        if (currentFragment is IncomeFragment) {
            (currentFragment as IncomeFragment).loadChartData()
            Log.d("StatsFragment", "loadChartData() called on IncomeFragment")
        } else if (currentFragment is ExpensesFragment) {
            (currentFragment as ExpensesFragment)
            Log.d("StatsFragment", "loadChartData() called on ExpensesFragment")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("showingIncome", showingIncome)
        Log.d("StatsFragment", "onSaveInstanceState() called, showingIncome: $showingIncome")
    }

    private fun loadFragment(fragment: Fragment, direction: String?) {
        Log.d("StatsFragment", "loadFragment() called with: $fragment")
        val transaction = childFragmentManager.beginTransaction()

        when (direction) {
            "left" -> transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            "right" -> transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            else -> transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        transaction.replace(R.id.fragmentContainer, fragment).commit()
        currentFragment = fragment
        Log.d("StatsFragment", "loadFragment() completed, currentFragment: $currentFragment")
    }

    private fun updateButtonUI() {
        Log.d("StatsFragment", "updateButtonUI() called, showingIncome: $showingIncome")
        if (showingIncome) {
            toggleButton.text = "Expenses"
            toggleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange_color))
        } else {
            toggleButton.text = "Income"
            toggleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.Accent_color))
        }
    }
}
