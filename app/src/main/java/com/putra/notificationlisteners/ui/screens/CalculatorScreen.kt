package com.putra.notificationlisteners.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putra.notificationlisteners.viewmodel.CalculatorViewModel

/**
 * Modern Calculator Screen inspired by iPhone calculator.
 *
 * Features:
 * - Clean, minimal design
 * - Rounded circular buttons
 * - Smooth press animations
 * - Dark theme
 * - Secret code detection (231199) → navigates to NotificationHistoryActivity
 *
 * Layout:
 * - Large display at top
 * - 4x5 button grid
 * - Color-coded buttons (numbers, operators, equals)
 *
 * @param onSecretCodeTriggered Callback when secret code (231199) is detected
 */
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    onSecretCodeTriggered: () -> Unit = {}
) {
    val display by viewModel.display.collectAsStateWithLifecycle()
    val secretCodeTriggered by viewModel.secretCodeTriggered.collectAsStateWithLifecycle()

    // Trigger navigation when secret code detected
    LaunchedEffect(secretCodeTriggered) {
        if (secretCodeTriggered) {
            viewModel.resetSecretCodeTrigger()
            onSecretCodeTriggered()
        }
    }

    // Color scheme (iPhone dark calculator)
    val backgroundColor = Color(0xFF1C1C1C)
    val displayBgColor = Color(0xFF1C1C1C)
    val numberButtonColor = Color(0xFF383838)
    val operatorButtonColor = Color(0xFFFF9500)
    val equalsButtonColor = Color(0xFF50C878)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = display,
                fontSize = 60.sp,
                fontWeight = FontWeight.Light,
                color = textColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }

        // Button Grid (4x5)
        // Row 1: C, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorButton(
                label = "C",
                backgroundColor = numberButtonColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onClear() }
            )
            CalculatorButton(
                label = "+/-",
                backgroundColor = numberButtonColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                onClick = { /* Future: implement sign toggle */ }
            )
            CalculatorButton(
                label = "%",
                backgroundColor = numberButtonColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                onClick = { /* Future: implement modulo */ }
            )
            CalculatorButton(
                label = "÷",
                backgroundColor = operatorButtonColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onOperator("÷") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorButton("7", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("7")
            }
            CalculatorButton("8", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("8")
            }
            CalculatorButton("9", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("9")
            }
            CalculatorButton("×", operatorButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onOperator("×")
            }
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorButton("4", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("4")
            }
            CalculatorButton("5", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("5")
            }
            CalculatorButton("6", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("6")
            }
            CalculatorButton("-", operatorButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onOperator("-")
            }
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorButton("1", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("1")
            }
            CalculatorButton("2", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("2")
            }
            CalculatorButton("3", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onNumberClick("3")
            }
            CalculatorButton("+", operatorButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onOperator("+")
            }
        }

        // Row 5: 0 (spans 2), ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorButton(
                "0",
                numberButtonColor,
                textColor,
                modifier = Modifier.weight(2f)
            ) {
                viewModel.onNumberClick("0")
            }
            CalculatorButton(".", numberButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onDecimal()
            }
            CalculatorButton("=", equalsButtonColor, textColor, Modifier.weight(1f)) {
                viewModel.onEquals()
            }
        }
    }
}

/**
 * Reusable calculator button component.
 *
 * Features:
 * - Press animation (scale down)
 * - Circular shape
 * - Customizable colors
 */
@Composable
private fun CalculatorButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val scale: Float by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "btn_scale")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = CircleShape,
                clip = false
            )
            .background(backgroundColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
        )
    }
}

// Extension function for aspect ratio
fun Modifier.aspectRatio(ratio: Float) = this.then(
    Modifier.fillMaxWidth()
)
