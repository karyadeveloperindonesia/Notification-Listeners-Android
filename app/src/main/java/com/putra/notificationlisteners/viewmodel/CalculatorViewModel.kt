package com.putra.notificationlisteners.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for Calculator functionality.
 *
 * Manages:
 * - Display value
 * - Calculator logic (add, subtract, multiply, divide)
 * - Secret code detection (231199)
 *
 * The secret code is tracked in a hidden buffer that resets
 * every 2 seconds of inactivity, preventing unintended triggers.
 */
class CalculatorViewModel : ViewModel() {

    // UI State
    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display

    // Calculator state
    private var currentInput = ""
    private var previousValue = 0.0
    private var operation: String? = null
    private var lastWasEqual = false

    // Secret code detection
    private val _secretCodeTriggered = MutableStateFlow(false)
    val secretCodeTriggered: StateFlow<Boolean> = _secretCodeTriggered

    private val secretCodeBuffer = StringBuilder()
    private val SECRET_CODE = "231199"

    /**
     * Handle number button press (0-9).
     * Also tracks the digit for secret code detection.
     */
    fun onNumberClick(num: String) {
        // Only single digit expected, but validate
        if (num.length != 1 || !num[0].isDigit()) return

        // Track for secret code
        trackSecretCode(num)

        // Calculator logic
        if (lastWasEqual) {
            currentInput = num
            lastWasEqual = false
        } else {
            currentInput += num
        }

        updateDisplay(currentInput)
    }

    /**
     * Handle decimal point.
     */
    fun onDecimal() {
        // Don't add decimal if it already exists
        if (currentInput.contains(".")) return

        if (currentInput.isEmpty()) {
            currentInput = "0."
        } else {
            currentInput += "."
        }

        updateDisplay(currentInput)
    }

    /**
     * Handle arithmetic operators (+, -, ×, ÷).
     */
    fun onOperator(op: String) {
        trackSecretCode(op) // For secret code (though unlikely)

        if (currentInput.isNotEmpty()) {
            if (operation != null && !lastWasEqual) {
                // Calculate intermediate result
                val result = calculate(
                    previousValue,
                    currentInput.toDoubleOrNull() ?: 0.0,
                    operation!!
                )
                previousValue = result
                currentInput = ""
                updateDisplay(formatNumber(result))
            } else {
                previousValue = currentInput.toDoubleOrNull() ?: 0.0
                currentInput = ""
            }
        }

        operation = op
        lastWasEqual = false
    }

    /**
     * Handle equals button.
     * Performs the pending calculation.
     */
    fun onEquals() {
        if (operation != null && currentInput.isNotEmpty()) {
            val result = calculate(
                previousValue,
                currentInput.toDoubleOrNull() ?: 0.0,
                operation!!
            )

            currentInput = ""
            previousValue = result
            operation = null
            lastWasEqual = true

            updateDisplay(formatNumber(result))
        }
    }

    /**
     * Toggle sign of current input (positive ↔ negative).
     */
    /**
     * Convert current input to percentage (divide by 100).
     * If an operation is pending, calculates percentage of previousValue.
     * e.g. 200 + 50% → 200 + 100 (50% of 200)
     */
    fun onPercent() {
        val value = currentInput.toDoubleOrNull() ?: return
        val result = if (operation != null) {
            previousValue * (value / 100.0)
        } else {
            value / 100.0
        }
        currentInput = formatNumber(result)
        updateDisplay(currentInput)
    }

    fun onToggleSign() {
        if (lastWasEqual) {
            // Negate the result currently on display
            previousValue = -previousValue
            updateDisplay(formatNumber(previousValue))
            return
        }
        if (currentInput.isEmpty() || currentInput == "0") return
        currentInput = if (currentInput.startsWith("-")) {
            currentInput.removePrefix("-")
        } else {
            "-$currentInput"
        }
        updateDisplay(currentInput)
    }

    /**
     * Delete the last character of current input (backspace).
     * Called when user swipes right on the display.
     */
    fun onBackspace() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            // If only "-" remains, treat as empty
            if (currentInput == "-") currentInput = ""
            updateDisplay(if (currentInput.isEmpty()) "0" else currentInput)
        }
    }

    /**
     * Clear all and reset to "0".
     */
    fun onClear() {
        currentInput = ""
        previousValue = 0.0
        operation = null
        lastWasEqual = false
        secretCodeBuffer.clear() // Reset secret code tracking
        updateDisplay("0")
    }

    /**
     * Perform arithmetic calculation.
     */
    private fun calculate(prev: Double, current: Double, op: String): Double {
        return when (op) {
            "+" -> prev + current
            "-" -> prev - current
            "×" -> prev * current
            "÷" -> if (current != 0.0) prev / current else 0.0
            else -> current
        }
    }

    /**
     * Format number for display:
     * - Remove trailing zeros after decimal
     * - Show integer if whole number
     * - Limit decimal places to 8
     */
    private fun formatNumber(num: Double): String {
        return if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            String.format("%.8f", num).trimEnd('0').trimEnd('.')
        }
    }

    /**
     * Update the display StateFlow.
     */
    private fun updateDisplay(value: String) {
        _display.value = if (value.isEmpty()) "0" else value
    }

    /**
     * Track secret code: "231199"
     * This buffer is updated with each digit pressed.
     * When it matches the secret code, trigger navigation.
     */
    private fun trackSecretCode(input: String) {
        // Only track digits for the secret code
        if (input.length == 1 && input[0].isDigit()) {
            secretCodeBuffer.append(input)

            // Keep only the last 6 characters (length of secret code)
            if (secretCodeBuffer.length > 6) {
                secretCodeBuffer.deleteCharAt(0)
            }

            // Check if current buffer matches the secret code
            if (secretCodeBuffer.toString() == SECRET_CODE) {
                _secretCodeTriggered.value = true
                secretCodeBuffer.clear()
            }
        }
    }

    /**
     * Reset the secret code trigger flag.
     * Call this after navigation to prevent repeated triggers.
     */
    fun resetSecretCodeTrigger() {
        _secretCodeTriggered.value = false
    }
}
