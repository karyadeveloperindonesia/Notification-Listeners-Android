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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putra.notificationlisteners.viewmodel.CalculatorViewModel

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
    val numberButtonColor = Color(0xFF383838)
    val operatorButtonColor = Color(0xFFFF9500)
    val equalsButtonColor = Color(0xFF50C878)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Area — top 45% of screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = display,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = textColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 16.dp)
            )
        }

        // Button Grid — bottom 55% of screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rowModifier = Modifier
                .fillMaxWidth()
                .weight(1f)

            // Row 1: C, +/-, %, ÷
            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("C", numberButtonColor, { viewModel.onClear() }),
                    Triple("+/-", numberButtonColor, {}),
                    Triple("%", numberButtonColor, {}),
                    Triple("÷", operatorButtonColor, { viewModel.onOperator("÷") })
                ).forEach { (label, color, action) ->
                    CalculatorButtonComponent(
                        label = label,
                        backgroundColor = color,
                        textColor = textColor,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = action
                    )
                }
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("7", numberButtonColor, { viewModel.onNumberClick("7") }),
                    Triple("8", numberButtonColor, { viewModel.onNumberClick("8") }),
                    Triple("9", numberButtonColor, { viewModel.onNumberClick("9") }),
                    Triple("×", operatorButtonColor, { viewModel.onOperator("×") })
                ).forEach { (label, color, action) ->
                    CalculatorButtonComponent(
                        label = label,
                        backgroundColor = color,
                        textColor = textColor,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = action
                    )
                }
            }

            // Row 3: 4, 5, 6, -
            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("4", numberButtonColor, { viewModel.onNumberClick("4") }),
                    Triple("5", numberButtonColor, { viewModel.onNumberClick("5") }),
                    Triple("6", numberButtonColor, { viewModel.onNumberClick("6") }),
                    Triple("-", operatorButtonColor, { viewModel.onOperator("-") })
                ).forEach { (label, color, action) ->
                    CalculatorButtonComponent(
                        label = label,
                        backgroundColor = color,
                        textColor = textColor,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = action
                    )
                }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("1", numberButtonColor, { viewModel.onNumberClick("1") }),
                    Triple("2", numberButtonColor, { viewModel.onNumberClick("2") }),
                    Triple("3", numberButtonColor, { viewModel.onNumberClick("3") }),
                    Triple("+", operatorButtonColor, { viewModel.onOperator("+") })
                ).forEach { (label, color, action) ->
                    CalculatorButtonComponent(
                        label = label,
                        backgroundColor = color,
                        textColor = textColor,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        onClick = action
                    )
                }
            }

            // Row 5: 0 (wide), ., =
            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButtonComponent(
                    label = "0",
                    backgroundColor = numberButtonColor,
                    textColor = textColor,
                    modifier = Modifier.weight(2f).fillMaxSize()
                ) { viewModel.onNumberClick("0") }

                CalculatorButtonComponent(
                    label = ".",
                    backgroundColor = numberButtonColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) { viewModel.onDecimal() }

                CalculatorButtonComponent(
                    label = "=",
                    backgroundColor = equalsButtonColor,
                    textColor = textColor,
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) { viewModel.onEquals() }
            }
        }
    }
}



/**
 * Reusable calculator button component.
 *
 * Features:
 * - Press animation (scale down)
 * - Rounded shape
 * - Customizable colors
 * - Fixed height with flexible width
 */
@Composable
private fun CalculatorButtonComponent(
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

    val scale: Float by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "btn_scale")

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
        )
    }
}
