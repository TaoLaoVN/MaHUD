package com.cpumonitor.core.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cpumonitor.core.designsystem.theme.MonitorColors

/**
 * Default chart styling helpers aligned with the design system palette.
 */
object MonitorChartDefaults {

    private val palette = listOf(
        MonitorColors.CpuUsage,
        MonitorColors.MemoryUsage,
        MonitorColors.Thermal,
        MonitorColors.CpuFrequency,
        MonitorColors.BatteryLevel,
        MonitorColors.StorageUsage,
    )

    @Composable
    fun fallbackLineColor(index: Int): Color =
        palette.getOrElse(index) { MaterialTheme.colorScheme.primary }
}
