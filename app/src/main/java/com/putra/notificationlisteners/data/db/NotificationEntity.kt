package com.putra.notificationlisteners.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a captured notification.
 *
 * Each row stores the metadata extracted from an incoming notification
 * intercepted by the NotificationListenerService. This data model forms
 * the foundation for offline storage and later forensic analysis.
 *
 * SECURITY NOTE: This table stores potentially sensitive information
 * (message bodies, sender names, etc.). In a production security tool,
 * consider encrypting the database with SQLCipher or Android's
 * EncryptedSharedPreferences equivalent for Room.
 */
@Entity(tableName = "captured_notifications")
data class NotificationEntity(

    /** Auto-generated primary key for each captured notification. */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Package name of the app that posted the notification.
     * Example: "com.whatsapp", "com.google.android.gm"
     * Useful for filtering and identifying the source application.
     */
    @ColumnInfo(name = "package_name")
    val packageName: String,

    /**
     * Human-readable label of the source application.
     * Resolved at capture time from the PackageManager.
     * Example: "WhatsApp", "Gmail"
     */
    @ColumnInfo(name = "app_name")
    val appName: String,

    /**
     * The notification title (android.title extra).
     * Typically the sender name or notification subject line.
     */
    @ColumnInfo(name = "title")
    val title: String,

    /**
     * The notification body text (android.text extra).
     * Contains the actual message content — the most sensitive field.
     */
    @ColumnInfo(name = "content")
    val content: String,

    /**
     * Unix timestamp (milliseconds) when the notification was captured.
     * Recorded at the moment onNotificationPosted() fires.
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
