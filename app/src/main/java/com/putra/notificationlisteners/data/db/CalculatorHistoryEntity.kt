package com.putra.notificationlisteners.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing calculator history entries.
 * Each row stores one completed calculation (e.g. "5+5=10").
 * Maximum 50 entries are kept — oldest are auto-pruned.
 */
@Entity(tableName = "calculator_history")
data class CalculatorHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** The expression string, e.g. "5+5" */
    @ColumnInfo(name = "expression")
    val expression: String,

    /** The result string, e.g. "10" */
    @ColumnInfo(name = "result")
    val result: String,

    /** Timestamp when the calculation was performed */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
