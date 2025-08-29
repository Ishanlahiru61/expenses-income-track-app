package com.example.expensetrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val amount: Double,
    val fromAccount: String,
    val toAccount: String,
    val note: String?
)
