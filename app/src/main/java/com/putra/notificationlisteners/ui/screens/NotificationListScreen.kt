package com.putra.notificationlisteners.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putra.notificationlisteners.data.db.NotificationEntity
import com.putra.notificationlisteners.service.NotificationCaptureService
import com.putra.notificationlisteners.ui.components.NotificationCard
import com.putra.notificationlisteners.viewmodel.NotificationViewModel

/**
 * Main screen of the Notification Capture application.
 *
 * Displays:
 * 1. Permission status banner (if notification access not granted)
 * 2. Statistics header (total captured count)
 * 3. Real-time scrollable list of captured notifications
 * 4. Clear all button in the top app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val count by viewModel.notificationCount.collectAsStateWithLifecycle()
    val isPermissionGranted = remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Refresh permission status when the screen is composed
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Notification Capture")
                        Text(
                            text = "Security Research Tool",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all notifications"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Permission Status Banner ─────────────────────────
            AnimatedVisibility(
                visible = !isPermissionGranted.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PermissionBanner(
                    onGrantClick = {
                        // Open the Notification Listener Settings page
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        )
                    },
                    onRefreshClick = {
                        isPermissionGranted.value = isNotificationListenerEnabled(context)
                    }
                )
            }

            // ── Statistics Header ────────────────────────────────
            if (isPermissionGranted.value) {
                StatsHeader(count = count)
            }

            // ── Notification List ────────────────────────────────
            if (notifications.isEmpty()) {
                EmptyState(isPermissionGranted = isPermissionGranted.value)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(notification = notification)
                    }
                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Clear All Confirmation Dialog ───────────────────────────
    if (showClearDialog) {
        ClearAllDialog(
            onConfirm = {
                viewModel.clearAllNotifications()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Sub-composables
// ═══════════════════════════════════════════════════════════════════

/**
 * Banner shown when notification listener permission is NOT granted.
 * Guides the user to the Settings page.
 */
@Composable
private fun PermissionBanner(
    onGrantClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notification Access Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This app needs Notification Access permission to capture " +
                        "notifications. Tap the button below to open Settings, then " +
                        "enable \"NotificationListeners\" in the list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onGrantClick, modifier = Modifier.weight(1f)) {
                    Text("Open Settings")
                }
                TextButton(onClick = onRefreshClick) {
                    Text("Refresh")
                }
            }
        }
    }
}

/**
 * Header showing the count of captured notifications.
 */
@Composable
private fun StatsHeader(count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Active",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Listener Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$count notifications captured",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Empty state shown when no notifications have been captured yet.
 */
@Composable
private fun EmptyState(isPermissionGranted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "No notifications",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isPermissionGranted) {
                    "No notifications captured yet"
                } else {
                    "Grant permission to start capturing"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPermissionGranted) {
                    "Notifications from other apps will appear here in real-time as they arrive."
                } else {
                    "Tap the \"Open Settings\" button above to enable Notification Access."
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Confirmation dialog for clearing all captured notifications.
 */
@Composable
private fun ClearAllDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        },
        title = { Text("Clear All Notifications") },
        text = {
            Text("This will permanently delete all captured notifications from the database. This action cannot be undone.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Clear All")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════
// Utility Functions
// ═══════════════════════════════════════════════════════════════════

/**
 * Checks whether our NotificationListenerService is currently enabled
 * in the device's Settings.
 *
 * This reads the secure setting "enabled_notification_listeners" which
 * contains a colon-separated list of ComponentName flatStrings for all
 * enabled notification listeners on the device.
 *
 * SECURITY NOTE: This setting is stored in Settings.Secure, which
 * requires the user to physically navigate to Settings to modify.
 * Apps cannot programmatically enable themselves as notification
 * listeners — this is a critical security safeguard.
 */
fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false

    if (TextUtils.isEmpty(flat)) return false

    val names = flat.split(":")
    for (name in names) {
        val cn = ComponentName.unflattenFromString(name)
        if (cn != null &&
            cn.packageName == context.packageName &&
            cn.className == NotificationCaptureService::class.java.name
        ) {
            return true
        }
    }
    return false
}
