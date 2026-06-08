package com.cpumonitor.core.designsystem.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes for responsive layouts across phones, tablets, and foldables.
 */
enum class MonitorWindowSize {
    /** Phone portrait, foldable cover screen. */
    Compact,

    /** Tablet portrait, foldable half-open. */
    Medium,

    /** Tablet landscape, foldable fully open. */
    Expanded,
    ;
}

val LocalMonitorWindowSize = staticCompositionLocalOf { MonitorWindowSize.Compact }

object MonitorBreakpoints {
    val compactMaxWidth: Dp = 600.dp
    val mediumMaxWidth: Dp = 840.dp
}

/**
 * Resolves [MonitorWindowSize] from the current max width.
 */
fun resolveMonitorWindowSize(maxWidth: Dp): MonitorWindowSize = when {
    maxWidth < MonitorBreakpoints.compactMaxWidth -> MonitorWindowSize.Compact
    maxWidth < MonitorBreakpoints.mediumMaxWidth -> MonitorWindowSize.Medium
    else -> MonitorWindowSize.Expanded
}

/**
 * Adaptive minimum cell width for dashboard-style grids.
 */
fun MonitorWindowSize.adaptiveGridMinSize(): Dp = when (this) {
    MonitorWindowSize.Compact -> 168.dp
    MonitorWindowSize.Medium -> 240.dp
    MonitorWindowSize.Expanded -> 200.dp
}

/**
 * Horizontal content padding tuned per window size.
 */
fun MonitorWindowSize.horizontalContentPadding(): Dp = when (this) {
    MonitorWindowSize.Compact -> 16.dp
    MonitorWindowSize.Medium -> 24.dp
    MonitorWindowSize.Expanded -> 32.dp
}

/**
 * Provides [LocalMonitorWindowSize] based on available width.
 */
@Composable
fun MonitorWindowSizeProvider(
    content: @Composable () -> Unit,
) {
    BoxWithConstraints {
        val windowSize = resolveMonitorWindowSize(maxWidth)
        CompositionLocalProvider(LocalMonitorWindowSize provides windowSize) {
            content()
        }
    }
}

/** Reads the current window size from [LocalMonitorWindowSize]. */
@Composable
fun rememberMonitorWindowSize(): MonitorWindowSize = LocalMonitorWindowSize.current
