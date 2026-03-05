package com.putra.notificationlisteners.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the captured_notifications table.
 *
 * Uses Kotlin Flow for reactive queries — the UI automatically updates
 * whenever a new notification is inserted into the database by the
 * background NotificationListenerService.
 */
@Dao
interface NotificationDao {

    /**
     * Observe all captured notifications, ordered newest-first.
     * Returns a Flow so that collectors (ViewModel → UI) receive
     * live updates whenever the underlying data changes.
     */
    @Query("SELECT * FROM captured_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    /**
     * Insert a newly captured notification into the database.
     * Uses REPLACE strategy to avoid crashes on unlikely PK conflicts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    /**
     * Delete all captured notifications (for a "Clear All" feature).
     */
    @Query("DELETE FROM captured_notifications")
    suspend fun deleteAll()

    /**
     * Get total count of captured notifications.
     * Useful for statistics / dashboard.
     */
    @Query("SELECT COUNT(*) FROM captured_notifications")
    fun getCount(): Flow<Int>
}
