package com.cpumonitor.core.designsystem.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class MonitorWindowSizeTest {

    @Test
    fun resolveMonitorWindowSize_mapsWidthBreakpoints() {
        assertEquals(MonitorWindowSize.Compact, resolveMonitorWindowSize(400.dp))
        assertEquals(MonitorWindowSize.Medium, resolveMonitorWindowSize(700.dp))
        assertEquals(MonitorWindowSize.Expanded, resolveMonitorWindowSize(1_000.dp))
    }

    @Test
    fun adaptiveGridMinSize_decreasesOnLargerScreens() {
        assertEquals(168.dp, MonitorWindowSize.Compact.adaptiveGridMinSize())
        assertEquals(240.dp, MonitorWindowSize.Medium.adaptiveGridMinSize())
        assertEquals(200.dp, MonitorWindowSize.Expanded.adaptiveGridMinSize())
    }
}
