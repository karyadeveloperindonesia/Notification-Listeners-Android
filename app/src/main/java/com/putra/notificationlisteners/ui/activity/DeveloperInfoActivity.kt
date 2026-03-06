package com.putra.notificationlisteners.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.putra.notificationlisteners.ui.screens.DeveloperInfoScreen
import com.putra.notificationlisteners.ui.theme.NotificationListenersTheme

class DeveloperInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationListenersTheme {
                val isDark = isSystemInDarkTheme()
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
                DeveloperInfoScreen(onBack = { finish() })
            }
        }
    }
}
