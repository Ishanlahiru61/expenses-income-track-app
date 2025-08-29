package com.example.expensetrack.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.expensetrack.model.*
import com.example.expensetrack.Data.*


@Database(
    entities = [
        TransferItem::class,
        IncomeItem::class,
        ExpenseItem::class,
        AccountBalance::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transferDao(): TransferDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountBalanceDao(): AccountBalanceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
