package com.putra.notificationlisteners.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.putra.notificationlisteners.data.db.AppDatabase
import com.putra.notificationlisteners.data.db.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Core service that intercepts all notifications posted on the device.
 *
 * ═══════════════════════════════════════════════════════════════════
 * HOW NotificationListenerService WORKS INTERNALLY
 * ═══════════════════════════════════════════════════════════════════
 *
 * 1. REGISTRATION: When the user grants "Notification Access" in
 *    Settings → Apps & notifications → Special app access →
 *    Notification access, the system binds to this service.
 *
 * 2. BINDING: Android's NotificationManagerService (a system service)
 *    establishes an IPC (Binder) connection to this service. The
 *    framework calls onListenerConnected() once the link is active.
 *
 * 3. INTERCEPTION: Every time any app posts a notification via
 *    NotificationManager.notify(), the system broadcasts the
 *    StatusBarNotification object to ALL registered listeners —
 *    including this service — by calling onNotificationPosted().
 *
 * 4. REMOVAL: When a notification is dismissed (swiped away or
 *    cancelled programmatically), onNotificationRemoved() fires.
 *
 * 5. CAPABILITIES: A listener can also:
 *    - Read the full Notification extras (title, text, bigText, etc.)
 *    - Dismiss notifications on behalf of the user
 *    - Snooze notifications
 *    - Access the notification's PendingIntent
 *
 * ═══════════════════════════════════════════════════════════════════
 * SECURITY IMPLICATIONS
 * ═══════════════════════════════════════════════════════════════════
 *
 * This API is extremely powerful and represents a significant attack
 * surface:
 *
 * • OTP/2FA THEFT: Notifications often contain one-time passwords
 *   from banking apps, email verification codes, and SMS 2FA tokens.
 *   A malicious listener can silently capture these in real-time.
 *
 * • MESSAGE ESPIONAGE: Chat apps (WhatsApp, Telegram, Signal) show
 *   message previews in notifications. A listener sees every message
 *   preview without needing to compromise the chat app itself.
 *
 * • FINANCIAL DATA: Banking apps show transaction alerts, balance
 *   notifications, and payment confirmations.
 *
 * • CREDENTIAL HARVESTING: Password reset emails and magic links
 *   often appear in notification previews.
 *
 * • SOCIAL ENGINEERING: By learning a user's contacts, habits, and
 *   communication patterns, an attacker can craft targeted phishing.
 *
 * This is why Android requires EXPLICIT user consent via Settings —
 * the permission CANNOT be granted programmatically or via a runtime
 * permission dialog, adding friction to prevent silent exploitation.
 */
class NotificationCaptureService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotifCaptureService"
    }

    /**
     * Coroutine scope tied to this service's lifecycle.
     * Uses SupervisorJob so a failure in one coroutine doesn't cancel others.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Lazily initialized Room DAO for database writes. */
    private val dao by lazy {
        AppDatabase.getInstance(applicationContext).notificationDao()
    }

    /**
     * Called when the listener is connected to the notification system.
     * At this point, we can start receiving notifications.
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "✓ Notification listener connected — now intercepting all notifications")
    }

    /**
     * Called when the listener is disconnected from the notification system.
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "✗ Notification listener disconnected")
    }

    /**
     * Called every time a new notification is posted by ANY app on the device.
     *
     * @param sbn The StatusBarNotification containing the full notification
     *            payload, including extras, package name, post time, key, etc.
     *
     * ATTACK VECTOR DEMONSTRATION:
     * This single callback gives us access to every notification on the device.
     * We extract the human-readable fields and persist them to a local database.
     * A real attacker could instead exfiltrate this data to a remote C2 server.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // Skip our own notifications to avoid infinite loops
        if (sbn.packageName == applicationContext.packageName) return

        try {
            val notification = sbn.notification
            val extras = notification.extras

            // Extract notification fields from the Bundle
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val packageName = sbn.packageName
            val timestamp = sbn.postTime

            // Resolve the human-readable application label
            val appName = resolveAppName(packageName)

            // Use big text if available (contains the full message body)
            val displayContent = bigText ?: content

            // Skip empty notifications (ongoing, progress bars, etc.)
            if (title.isBlank() && displayContent.isBlank()) return

            // Build the entity for database persistence
            val entity = NotificationEntity(
                packageName = packageName,
                appName = appName,
                title = title,
                content = displayContent,
                timestamp = timestamp
            )

            // Log to Logcat for real-time monitoring via ADB
            Log.d(
                TAG,
                """
                |╔══════════════════════════════════════════
                |║ CAPTURED NOTIFICATION
                |╠══════════════════════════════════════════
                |║ App:     $appName ($packageName)
                |║ Title:   $title
                |║ Content: $displayContent
                |║ Time:    $timestamp
                |╚══════════════════════════════════════════
                """.trimMargin()
            )

            // Persist to Room database asynchronously
            serviceScope.launch {
                try {
                    dao.insert(entity)
                    Log.d(TAG, "Notification persisted to database [id will be auto-assigned]")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist notification", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification from ${sbn.packageName}", e)
        }
    }

    /**
     * Called when a notification is removed/dismissed.
     * Logged for completeness — could be used to track notification lifetimes.
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        Log.d(TAG, "Notification removed: ${sbn.packageName} / ${sbn.key}")
    }

    /**
     * Resolves a package name to a human-readable application label.
     * Falls back to the package name itself if resolution fails.
     */
    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName // Fallback to package name
        }
    }

    /**
     * Cleanup: Cancel all running coroutines when the service is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "NotificationCaptureService destroyed, coroutines cancelled")
    }
}
