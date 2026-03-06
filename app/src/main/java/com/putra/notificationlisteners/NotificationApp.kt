package com.putra.notificationlisteners

import android.app.Application
import com.putra.notificationlisteners.data.db.AppDatabase
import com.putra.notificationlisteners.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class for the Notification Capture app.
 *
 * Serves as the application-level context holder. The Room database
 * singleton is initialized lazily on first access using this context.
 *
 * In a more complex app, this would be the place for dependency
 * injection setup (Hilt/Dagger), WorkManager initialization, or
 * Timber logging configuration.
 */
class NotificationApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Auto-cleanup: remove notifications older than 7 days on every app launch
        appScope.launch {
            try {
                val dao = AppDatabase.getInstance(this@NotificationApp).notificationDao()
                val repo = NotificationRepository(dao)
                repo.cleanupOldNotifications()
            } catch (_: Exception) { }
        }
    }
}
