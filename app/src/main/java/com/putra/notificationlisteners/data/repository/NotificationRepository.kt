package com.putra.notificationlisteners.data.repository

import com.putra.notificationlisteners.data.db.NotificationDao
import com.putra.notificationlisteners.data.db.NotificationEntity
import kotlinx.coroutines.flow.Flow

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
}
