package com.example.expensetrack

import android.content.Context
import com.example.expensetrack.Database.AppDatabase
import com.example.expensetrack.model.AccountBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AccountManager {

    suspend fun getBalance(context: Context, account: String): Double = withContext(Dispatchers.IO) {
        val acc = normalize(account)
        val dao = AppDatabase.getDatabase(context).accountBalanceDao()
        dao.getAccount(acc)?.balance ?: 0.0
    }

    suspend fun setBalance(context: Context, account: String, amount: Double) = withContext(Dispatchers.IO) {
        val acc = normalize(account)
        val dao = AppDatabase.getDatabase(context).accountBalanceDao()
        dao.insertOrUpdate(AccountBalance(acc, amount))
    }

    suspend fun addAmount(context: Context, account: String, amount: Double) {
        val current = getBalance(context, account)
        setBalance(context, account, current + amount)
    }

    suspend fun deductAmount(context: Context, account: String, amount: Double) {
        val current = getBalance(context, account)
        setBalance(context, account, current - amount)
    }

    suspend fun resetAll(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).accountBalanceDao().clearAll()
    }

    private fun normalize(account: String): String = when (account) {
        "Bank", "Bank Account" -> "Bank"
        "Cash", "Petty Cash" -> "Cash"
        "Credit Card", "Card" -> "Card"
        else -> account
    }
}
