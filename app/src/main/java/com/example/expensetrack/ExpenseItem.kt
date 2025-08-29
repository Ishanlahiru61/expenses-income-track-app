package com.example.expensetrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val amount: Double,
    val account: String,
    val date: String,
    val color: Int,
    val note: String? = null
)
