package com.example.expensetrack

data class Transfer(
    val date: String,
    val amount: Double,
    val from: String,
    val to: String,
    val note: String?
)