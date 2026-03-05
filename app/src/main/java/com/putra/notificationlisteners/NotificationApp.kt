package com.putra.notificationlisteners

import android.app.Application

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

    override fun onCreate() {
        super.onCreate()
        // Database is initialized lazily via AppDatabase.getInstance()
        // No explicit initialization needed here
    }
}
