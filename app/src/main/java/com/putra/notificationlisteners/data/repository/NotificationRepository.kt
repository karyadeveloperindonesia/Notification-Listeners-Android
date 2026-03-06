package com.putra.notificationlisteners.data.repository

import com.putra.notificationlisteners.data.db.NotificationDao
import com.putra.notificationlisteners.data.db.NotificationEntity
import kotlinx.coroutines.flow.Flow

private const val RETENTION_DAYS = 7L
private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

/**
 * Repository layer that abstracts data access for the ViewModel.
 *
 * In MVVM architecture, the Repository acts as a single source of truth.
 * It sits between the ViewModel (business logic) and the DAO (data access),
 * making the code testable and decoupled.
 *
 * Currently backed solely by Room (local SQLite). In a real attack scenario,
 * an attacker might add a second data source here that exfiltrates captured
 * notifications to a remote server — this is why notification access is
 * such a dangerous permission.
 */
class NotificationRepository(private val dao: NotificationDao) {

    /**
     * Reactive stream of all captured notifications, newest first.
     * The Flow automatically emits new values when the database changes.
     */
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    /**
     * Reactive count of total captured notifications.
     */
    val notificationCount: Flow<Int> = dao.getCount()

    /**
     * Delete all captured notifications from the database.
     */
    suspend fun deleteAll() {
        dao.deleteAll()
    }

    /**
     * Insert a single notification (used by the service).
     */
    suspend fun insert(notification: NotificationEntity) {
        dao.insert(notification)
    }

    /**
     * Delete notifications older than 7 days.
     * Call on app start and periodically from the service.
     */
    suspend fun cleanupOldNotifications() {
        val threshold = System.currentTimeMillis() - (RETENTION_DAYS * MILLIS_PER_DAY)
        dao.deleteOlderThan(threshold)
    }
}
