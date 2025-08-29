package com.example.expensetrack.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.expensetrack.model.ExpenseItem

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseItem)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    suspend fun getAllExpenses(): List<ExpenseItem>
}
