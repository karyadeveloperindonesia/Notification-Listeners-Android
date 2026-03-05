package com.putra.notificationlisteners.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.putra.notificationlisteners.ui.screens.NotificationListScreen
import com.putra.notificationlisteners.ui.theme.NotificationListenersTheme

/**
 * Activity for displaying the notification history.
 *
 * This is the hidden page that shows all captured notifications.
 * Accessible only by entering the secret code (231199) in the calculator.
 *
 * SECURITY NOTE: In a real attack scenario, this activity would be
 * exfiltrating data to a remote server instead of displaying locally.
 */
class NotificationHistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotificationListenersTheme {
                NotificationListScreen(onBack = { finish() })
            }
        }
    }
}
