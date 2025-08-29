package com.example.expensetrack

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _expenseAmount = MutableLiveData<Double>()
    val expenseAmount: LiveData<Double> = _expenseAmount

    fun sendExpense(amount: Double) {
        _expenseAmount.value = amount
    }
}
