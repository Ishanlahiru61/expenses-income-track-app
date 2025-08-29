package com.example.expensetrack

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AccountsFragment : Fragment() {

    private lateinit var bankBalanceText: TextView
    private lateinit var cashBalanceText: TextView
    private lateinit var cardBalanceText: TextView
    private lateinit var totalBalanceText: TextView
    private lateinit var setBudgetBtn: Button
    private lateinit var modifyBudgetBtn: Button
    private lateinit var budgetEdit: EditText
    private lateinit var budgetForm: LinearLayout
    private lateinit var budgetCard: LinearLayout
    private lateinit var currentBudgetText: TextView
    private lateinit var currentSpentText: TextView
    private lateinit var remainingText: TextView
    private lateinit var percentageText: TextView

    private var monthlyBudget = 0.0
    private var spent = 0.0
    private var usedPct = 0.0

    companion object {
        private const val PREFS_NAME = "BudgetPrefs"
        private const val BUDGET_KEY = "monthly_budget"
        private const val SPENT_KEY = "current_spent_amount"
    }

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        loadSavedBudget()
        Log.d("AccountsFragment", "onCreate: Budget=$monthlyBudget, Spent=$spent")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_accounts, container, false)
        bindViews(view)

        setupUI()
        setupListeners()
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        parentFragmentManager.setFragmentResultListener("update_accounts", viewLifecycleOwner) { _, _ ->
            refreshBalances()
        }
    }

    private fun bindViews(v: View) {
        bankBalanceText    = v.findViewById(R.id.bankBalanceText)
        cashBalanceText    = v.findViewById(R.id.cashBalanceText)
        cardBalanceText    = v.findViewById(R.id.creditCardBalanceText)
        totalBalanceText   = v.findViewById(R.id.totalBalanceText)
        setBudgetBtn       = v.findViewById(R.id.setBudgetButton)
        modifyBudgetBtn    = v.findViewById(R.id.modifyBudgetButton)
        budgetEdit         = v.findViewById(R.id.budgetEditText)
        budgetForm         = v.findViewById(R.id.budgetForm)
        budgetCard         = v.findViewById(R.id.budgetCard)
        currentBudgetText  = v.findViewById(R.id.currentBudgetText)
        currentSpentText   = v.findViewById(R.id.currentAmountText)
        remainingText      = v.findViewById(R.id.remainingText)
        percentageText     = v.findViewById(R.id.percentageText)
    }

    private fun setupUI() {
        if (monthlyBudget > 0) showBudgetCard() else showBudgetForm()
        refreshBalances()
        recalcAndDrawBudget()
    }

    private fun setupListeners() {
        setBudgetBtn.setOnClickListener { handleBudgetSet() }
        modifyBudgetBtn.setOnClickListener { showBudgetForm() }


        sharedViewModel.expenseAmount.observe(viewLifecycleOwner) { amt ->
            if (amt > 0) {
                Log.d("AccountsFragment", "Got expense: $amt")
                spent += amt
                recalcAndDrawBudget()
                saveBudget()

                refreshBalances()
            }
        }
    }

    private fun handleBudgetSet() {
        val input = budgetEdit.text.toString().trim()
        when {
            input.isEmpty()               -> budgetEdit.error = "Please enter a budget"
            input.toDoubleOrNull() == null -> budgetEdit.error = "Invalid number"
            input.toDouble() <= 0         -> budgetEdit.error = "Must be > 0"
            else -> {
                monthlyBudget = input.toDouble()
                spent = 0.0
                recalcAndDrawBudget()
                saveBudget()
                showBudgetCard()
                Toast.makeText(requireContext(), "Budget set", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshBalances() {
        lifecycleScope.launch {
            val bank = AccountManager.getBalance(requireContext(), "Bank")
            val cash = AccountManager.getBalance(requireContext(), "Cash")
            val card = AccountManager.getBalance(requireContext(), "Credit Card")
            val total = bank + cash + card

            bankBalanceText.text  = "Bank: Rs%.2f".format(bank)
            cashBalanceText.text  = "Cash: Rs%.2f".format(cash)
            cardBalanceText.text  = "Card: Rs%.2f".format(card)
            totalBalanceText.text = "Total: Rs%.2f".format(total)
        }
    }

    private fun recalcAndDrawBudget() {
        usedPct = if (monthlyBudget > 0) (spent / monthlyBudget) * 100 else 0.0
        currentBudgetText.text = "Budget: Rs%.2f".format(monthlyBudget)
        currentSpentText.text  = "Spent: Rs%.2f".format(spent)
        val rem = monthlyBudget - spent
        remainingText.text = "Remaining: Rs%.2f".format(rem)
        percentageText.text = "Used: %.2f%%".format(usedPct)

        val col = when {
            usedPct >= 100 -> R.color.Accent_color
            usedPct >= 90  -> R.color.orange_color
            usedPct >= 50  -> R.color.light_gray
            else           -> R.color.Accent_color
        }
        percentageText.setTextColor(ContextCompat.getColor(requireContext(), col))
    }

    private fun loadSavedBudget() {
        val p = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        monthlyBudget    = p.getFloat(BUDGET_KEY, 0f).toDouble()
        spent            = p.getFloat(SPENT_KEY, 0f).toDouble()
    }

    private fun saveBudget() {
        requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(BUDGET_KEY, monthlyBudget.toFloat())
            .putFloat(SPENT_KEY, spent.toFloat())
            .apply()
    }

    private fun showBudgetForm() {
        budgetForm.visibility = View.VISIBLE
        budgetCard.visibility = View.GONE
        budgetEdit.setText(if (monthlyBudget > 0) monthlyBudget.toString() else "")
    }

    private fun showBudgetCard() {
        budgetForm.visibility = View.GONE
        budgetCard.visibility = View.VISIBLE
    }
}
