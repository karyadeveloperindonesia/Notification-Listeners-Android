package com.putra.notificationlisteners.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for storing captured notifications.
 *
 * Singleton pattern ensures only one database instance exists across
 * the entire application lifecycle — critical because both the
 * NotificationListenerService (running in its own lifecycle) and the
 * Activity/ViewModel need to share the same database connection.
 *
 * SECURITY NOTE: In a real-world scenario, an attacker's app would
 * use a similar database to silently accumulate notification data.
 * The database file is stored in the app's private internal storage
 * (/data/data/<pkg>/databases/), which is sandboxed by Android —
 * but the app itself has full access to read/export this data.
 */
@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance, creating it if needed.
         * Uses double-checked locking for thread safety.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notification_capture_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
