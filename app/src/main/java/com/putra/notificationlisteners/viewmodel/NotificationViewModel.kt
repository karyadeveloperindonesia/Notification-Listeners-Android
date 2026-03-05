package com.putra.notificationlisteners.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.putra.notificationlisteners.data.db.AppDatabase
import com.putra.notificationlisteners.data.db.NotificationEntity
import com.putra.notificationlisteners.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the notification capture screen.
 *
 * Follows the MVVM (Model-View-ViewModel) pattern:
 * - Model:     NotificationEntity + Room database
 * - ViewModel: This class — exposes UI state as StateFlow
 * - View:      Compose UI that collects the StateFlow
 *
 * Uses AndroidViewModel to access the Application context needed
 * for initializing the Room database singleton.
 *
 * The StateFlow is configured with WhileSubscribed(5000) to keep
 * the upstream Flow active for 5 seconds after the last subscriber
 * detaches (e.g., during configuration changes like screen rotation),
 * avoiding unnecessary database re-queries.
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository

    /** Reactive list of all captured notifications for the UI to display. */
    val notifications: StateFlow<List<NotificationEntity>>

    /** Reactive count of total captured notifications. */
    val notificationCount: StateFlow<Int>

    init {
        val dao = AppDatabase.getInstance(application).notificationDao()
        repository = NotificationRepository(dao)

        notifications = repository.allNotifications
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

        notificationCount = repository.notificationCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )
    }

    /**
     * Clear all captured notifications from the database.
     * Launches in viewModelScope to ensure proper lifecycle management.
     */
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
