package com.example.expensetrack.Data

import androidx.room.*
import com.example.expensetrack.model.IncomeItem

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(incomeItem: IncomeItem)

    @Query("SELECT * FROM income")
    suspend fun getAllIncomes(): List<IncomeItem>
}
