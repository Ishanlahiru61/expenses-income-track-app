package com.example.expensetrack.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.expensetrack.model.TransferItem

@Dao
interface TransferDao {
    @Insert
    suspend fun insertTransfer(item: TransferItem)

    @Query("SELECT * FROM transfers ORDER BY id DESC")
    suspend fun getAllTransfers(): List<TransferItem>
}
