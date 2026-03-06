package com.putra.notificationlisteners.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.putra.notificationlisteners.data.db.NotificationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, accentColor) = getCardColors(notification.packageName, isDark)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Colored circle icon badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // App name + time row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.appName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.65f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                if (notification.title.isNotBlank()) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Content body
                if (notification.content.isNotBlank()) {
                    Text(
                        text = notification.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor.copy(alpha = 0.72f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Package name pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accentColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = notification.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Deterministic card color palette derived from the package name, adapts to dark/light theme. */
private fun getCardColors(packageName: String, isDark: Boolean): Pair<Color, Color> {
    val lightPalettes = listOf(
        Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)), // Blue
        Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)), // Green
        Pair(Color(0xFFFFF3E0), Color(0xFFE65100)), // Orange
        Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A)), // Purple
        Pair(Color(0xFFFFEBEE), Color(0xFFC62828)), // Red
        Pair(Color(0xFFE0F2F1), Color(0xFF00695C)), // Teal
        Pair(Color(0xFFFFFDE7), Color(0xFFF57F17)), // Amber
        Pair(Color(0xFFE8EAF6), Color(0xFF283593)), // Indigo
        Pair(Color(0xFFFCE4EC), Color(0xFF880E4F)), // Pink
        Pair(Color(0xFFF9FBE7), Color(0xFF558B2F)), // Light Green
    )
    val darkPalettes = listOf(
        Pair(Color(0xFF0D2137), Color(0xFF90CAF9)), // Blue
        Pair(Color(0xFF0D2115), Color(0xFFA5D6A7)), // Green
        Pair(Color(0xFF2B1A05), Color(0xFFFFCC80)), // Orange
        Pair(Color(0xFF1E0D2E), Color(0xFFCE93D8)), // Purple
        Pair(Color(0xFF2D0A0A), Color(0xFFEF9A9A)), // Red
        Pair(Color(0xFF071F1C), Color(0xFF80CBC4)), // Teal
        Pair(Color(0xFF2B2200), Color(0xFFFFE082)), // Amber
        Pair(Color(0xFF0D1030), Color(0xFF9FA8DA)), // Indigo
        Pair(Color(0xFF2D0A1A), Color(0xFFF48FB1)), // Pink
        Pair(Color(0xFF141F07), Color(0xFFAED581)), // Light Green
    )
    val index = Math.abs(packageName.hashCode()) % lightPalettes.size
    return if (isDark) darkPalettes[index] else lightPalettes[index]
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
