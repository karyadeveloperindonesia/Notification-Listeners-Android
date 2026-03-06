package com.putra.notificationlisteners.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
        setContent {
            NotificationListenersTheme {
                val isDark = isSystemInDarkTheme()
                // Re-apply system bar style whenever dark theme changes
                enableEdgeToEdge(
                    statusBarStyle = if (isDark)
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (isDark)
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                )
                NotificationListScreen(onBack = { finish() })
            }
        }
    }
}
