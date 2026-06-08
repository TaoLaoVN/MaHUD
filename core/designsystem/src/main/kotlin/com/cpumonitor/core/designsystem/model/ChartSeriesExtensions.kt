package com.cpumonitor.core.designsystem.model

/**
 * Returns a new [ChartSeriesUiState] with [value] appended and rolling window applied.
 */
fun ChartSeriesUiState.appendSample(value: Float): ChartSeriesUiState {
    val updated = (values + value).takeLast(maxPoints)
    return copy(values = updated)
}

/**
 * Formats a percentage metric for display.
 */
fun Float.toPercentDisplay(decimals: Int = 1): String =
    "%.${decimals}f".format(this)

/**
 * Formats frequency in MHz for display.
 */
fun Float.toFrequencyMhzDisplay(): String = "%.0f".format(this)
