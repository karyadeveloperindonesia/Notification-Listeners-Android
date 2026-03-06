package com.putra.notificationlisteners.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculatorHistoryDao {

    /** Get all history entries, newest first. */
    @Query("SELECT * FROM calculator_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CalculatorHistoryEntity>>

    /** Insert a new history entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CalculatorHistoryEntity)

    /** Get total row count (for pruning). */
    @Query("SELECT COUNT(*) FROM calculator_history")
    suspend fun getCount(): Int

    /** Delete the oldest entry (smallest timestamp). */
    @Query("DELETE FROM calculator_history WHERE id = (SELECT id FROM calculator_history ORDER BY timestamp ASC LIMIT 1)")
    suspend fun deleteOldest()

    /** Delete all history. */
    @Query("DELETE FROM calculator_history")
    suspend fun deleteAll()
}
