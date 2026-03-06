package com.putra.notificationlisteners.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putra.notificationlisteners.data.db.NotificationEntity
import com.putra.notificationlisteners.service.NotificationCaptureService
import com.putra.notificationlisteners.ui.activity.DeveloperInfoActivity
import com.putra.notificationlisteners.ui.components.NotificationCard
import com.putra.notificationlisteners.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun NotificationListScreen(
    viewModel: NotificationViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val count by viewModel.notificationCount.collectAsStateWithLifecycle()
    val isPermissionGranted = remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Group notifications by date label
    val grouped = remember(notifications) { groupByDate(notifications) }

    // Expanded state per section — default all expanded
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // ── Top Bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular back button — iOS 26 / Android 17 style
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (count > 0) {
                    Text(
                        text = "$count captured",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Circular delete button
            if (notifications.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { showClearDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear all",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Developer info button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        context.startActivity(Intent(context, DeveloperInfoActivity::class.java))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Developer Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Permission Banner ────────────────────────────────────
        AnimatedVisibility(
            visible = !isPermissionGranted.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            PermissionBanner(
                onGrantClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                onRefreshClick = {
                    isPermissionGranted.value = isNotificationListenerEnabled(context)
                }
            )
        }

        // ── Stats Header ─────────────────────────────────────────
        if (isPermissionGranted.value && notifications.isNotEmpty()) {
            StatsHeader(count = count)
        }

        // ── Content ──────────────────────────────────────────────
        if (notifications.isEmpty()) {
            EmptyState(isPermissionGranted = isPermissionGranted.value)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (dateLabel, items) ->
                    val isExpanded = expandedSections.getOrDefault(dateLabel, true)

                    item(key = "header_$dateLabel") {
                        DateSectionHeader(
                            date = dateLabel,
                            count = items.size,
                            isExpanded = isExpanded,
                            onToggle = { expandedSections[dateLabel] = !isExpanded }
                        )
                    }

                    if (isExpanded) {
                        items(items = items, key = { it.id }) { notification ->
                            NotificationCard(notification = notification)
                        }
                    }

                    item(key = "spacer_$dateLabel") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
    } // end Surface

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

// ═══════════════════════════════════════════════════════════════
// Sub-composables
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DateSectionHeader(
    date: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                              else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PermissionBanner(
    onGrantClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
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
                text = "Enable Notification Access in Settings to start capturing notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun StatsHeader(count: Int) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0D2115) else Color(0xFFE8F5E9)
    val greenColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(greenColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Listener Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = greenColor.copy(alpha = 0.7f)
                )
                Text(
                    text = "$count notifications captured",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = greenColor
                )
            }
        }
    }
}

@Composable
private fun EmptyState(isPermissionGranted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isPermissionGranted) "No notifications yet"
                       else "Grant permission to start",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPermissionGranted) "Notifications will appear here as they arrive."
                       else "Tap \"Open Settings\" above to enable access.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClearAllDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
        title = { Text("Clear All") },
        text = { Text("Delete all captured notifications? This cannot be undone.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Clear All") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ═══════════════════════════════════════════════════════════════
// Utility
// ═══════════════════════════════════════════════════════════════

private fun groupByDate(notifications: List<NotificationEntity>): Map<String, List<NotificationEntity>> {
    val sdfKey   = SimpleDateFormat("yyyyMMdd",      Locale.getDefault())
    val sdfLabel = SimpleDateFormat("MMMM d, yyyy",  Locale.getDefault())
    val todayKey = sdfKey.format(Date())
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayKey = sdfKey.format(cal.time)

    return notifications.groupBy { notif ->
        val key = sdfKey.format(Date(notif.timestamp))
        when (key) {
            todayKey     -> "Today"
            yesterdayKey -> "Yesterday"
            else         -> sdfLabel.format(Date(notif.timestamp))
        }
    }
}

fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false

    if (TextUtils.isEmpty(flat)) return false

    return flat.split(":").any { name ->
        val cn = ComponentName.unflattenFromString(name)
        cn != null &&
            cn.packageName == context.packageName &&
            cn.className == NotificationCaptureService::class.java.name
    }
}

