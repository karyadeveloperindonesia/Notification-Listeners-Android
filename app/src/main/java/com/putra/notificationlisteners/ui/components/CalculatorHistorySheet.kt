package com.putra.notificationlisteners.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.putra.notificationlisteners.R
import com.putra.notificationlisteners.data.db.CalculatorHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sfProRounded = FontFamily(Font(R.font.sf_pro_rounded_semibold))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorHistorySheet(
    histories: List<CalculatorHistoryEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetBg = Color(0xFF2C2C2E)
    val cardBg = Color(0xFF3A3A3C)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF98989F)
    val accentGreen = Color(0xFF50C878)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textSecondary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "History",
                    fontSize = 18.sp,
                    fontFamily = sfProRounded,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${histories.size} calculations",
                    fontSize = 13.sp,
                    fontFamily = sfProRounded,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))
            }
        }
    ) {
        if (histories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history yet",
                    fontSize = 15.sp,
                    fontFamily = sfProRounded,
                    color = textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(histories, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentGreen = accentGreen
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: CalculatorHistoryEntity,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentGreen: Color
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(entry.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expression + result
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.expression,
                fontSize = 15.sp,
                fontFamily = sfProRounded,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "= ${entry.result}",
                fontSize = 22.sp,
                fontFamily = sfProRounded,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accentGreen.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = timeStr,
                fontSize = 12.sp,
                fontFamily = sfProRounded,
                color = accentGreen
            )
        }
    }
}
