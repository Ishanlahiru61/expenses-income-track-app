
package com.example.expensetrack.Data

import androidx.room.*
import com.example.expensetrack.model.AccountBalance

@Dao
interface AccountBalanceDao {
    @Query("SELECT * FROM account_balances WHERE account = :account LIMIT 1")
    suspend fun getAccount(account: String): AccountBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: AccountBalance)

    @Query("UPDATE account_balances SET balance = :balance WHERE account = :account")
    suspend fun updateBalance(account: String, balance: Double)

    @Query("DELETE FROM account_balances")
    suspend fun clearAll()
}
