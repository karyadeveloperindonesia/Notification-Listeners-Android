package com.putra.notificationlisteners

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.putra.notificationlisteners.ui.screens.NotificationListScreen
import com.putra.notificationlisteners.ui.theme.NotificationListenersTheme

/**
 * Main entry point of the Notification Capture application.
 *
 * This Activity hosts the Compose UI and serves as the launching point
 * for the security research tool. The actual notification interception
 * happens in NotificationCaptureService (a bound system service), while
 * this Activity provides the user interface for:
 *
 * 1. Checking/requesting notification listener permission
 * 2. Displaying captured notifications in real-time
 * 3. Managing (clearing) the captured notification database
 *
 * ARCHITECTURE OVERVIEW (MVVM):
 * ┌─────────────┐    ┌──────────────┐    ┌────────────┐    ┌──────────┐
 * │  Compose UI  │ ←→ │  ViewModel   │ ←→ │ Repository │ ←→ │ Room DB  │
 * │  (View)      │    │              │    │            │    │ (SQLite) │
 * └─────────────┘    └──────────────┘    └────────────┘    └──────────┘
 *                                                                ↑
 *                                     ┌──────────────────────────┘
 *                                     │
 *                          ┌──────────┴──────────┐
 *                          │ NotificationListener │
 *                          │ Service (writes)     │
 *                          └─────────────────────┘
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotificationListenersTheme {
                NotificationListScreen()
            }
        }
    }
}