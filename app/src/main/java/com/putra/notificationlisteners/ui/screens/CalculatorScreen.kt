package com.putra.notificationlisteners.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.putra.notificationlisteners.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.putra.notificationlisteners.viewmodel.CalculatorViewModel

private val sfProRounded = FontFamily(Font(R.font.sf_pro_rounded_semibold))

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Area — top 40% of screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    var triggered = false
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f; triggered = false },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                            // Trigger backspace once per swipe, threshold 60px rightward
                            if (totalDrag > 60f && !triggered) {
                                triggered = true
                                viewModel.onBackspace()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            // Font size based on character count — reliable, no re-layout loops
            val fontSize = when {
                display.length <= 6  -> 72.sp
                display.length <= 8  -> 58.sp
                display.length <= 10 -> 46.sp
                display.length <= 12 -> 36.sp
                else                 -> 28.sp
            }

            Text(
                text = display,
                fontSize = fontSize,
                fontFamily = sfProRounded,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 16.dp)
            )
        }

        // Button Grid — bottom 60% of screen
        // Use BoxWithConstraints to derive exact button size from available width
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f)
        ) {
            val spacing = 8.dp
            val buttonSize = (maxWidth - spacing * 3) / 4

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically)
            ) {
                // Row 1: C, +/-, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    listOf(
                        Triple("C", numberButtonColor, { viewModel.onClear() }),
                        Triple("+/-", numberButtonColor, { viewModel.onToggleSign() }),
                        Triple("%", numberButtonColor, { viewModel.onPercent() }),
                        Triple("÷", operatorButtonColor, { viewModel.onOperator("÷") })
                    ).forEach { (label, color, action) ->
                        CalculatorButtonComponent(
                            label = label,
                            backgroundColor = color,
                            textColor = textColor,
                            modifier = Modifier.size(buttonSize),
                            onClick = action
                        )
                    }
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
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
                            modifier = Modifier.size(buttonSize),
                            onClick = action
                        )
                    }
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
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
                            modifier = Modifier.size(buttonSize),
                            onClick = action
                        )
                    }
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
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
                            modifier = Modifier.size(buttonSize),
                            onClick = action
                        )
                    }
                }

                // Row 5: 0 (wide pill), ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    CalculatorButtonComponent(
                        label = "0",
                        backgroundColor = numberButtonColor,
                        textColor = textColor,
                        modifier = Modifier.size(width = buttonSize * 2 + spacing, height = buttonSize)
                    ) { viewModel.onNumberClick("0") }

                    CalculatorButtonComponent(
                        label = ".",
                        backgroundColor = numberButtonColor,
                        textColor = textColor,
                        modifier = Modifier.size(buttonSize)
                    ) { viewModel.onDecimal() }

                    CalculatorButtonComponent(
                        label = "=",
                        backgroundColor = equalsButtonColor,
                        textColor = textColor,
                        modifier = Modifier.size(buttonSize)
                    ) { viewModel.onEquals() }
                }
            }
        }
    }
    } // close Box
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
                is PressInteraction.Press   -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel  -> isPressed = false
            }
        }
    }

    // Spring scale: snappy press-down, bouncy release
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "btn_scale"
    )

    // Light flash overlay: fast in, smooth fade out
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.28f else 0f,
        animationSpec = if (isPressed)
            tween(durationMillis = 60)
        else
            tween(durationMillis = 300),
        label = "btn_overlay"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Light flash overlay on top of background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )
        Text(
            text = label,
            fontSize = 31.sp,
            fontFamily = sfProRounded,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
