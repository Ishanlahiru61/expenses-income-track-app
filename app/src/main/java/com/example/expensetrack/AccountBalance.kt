package com.example.expensetrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_balances")
data class AccountBalance(
    @PrimaryKey val account: String,
    val balance: Double
)
