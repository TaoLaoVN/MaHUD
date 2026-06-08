package com.cpumonitor.feature.dashboard.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cpumonitor.core.designsystem.model.ChartSeriesUiState
import com.cpumonitor.core.designsystem.model.MetricValueUiState
import com.cpumonitor.core.designsystem.model.MonitorWidgetUiState
import com.cpumonitor.core.designsystem.theme.CPUMonitorTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardContent_displaysWidgetsAndQuickAccess() {
        val widgets = listOf(
            MonitorWidgetUiState(
                id = "cpu",
                title = "CPU Usage",
                primaryMetric = MetricValueUiState(
                    label = "Total",
                    value = "42",
                    unit = "%",
                ),
                chartSeries = ChartSeriesUiState(
                    id = "cpu_chart",
                    label = "CPU",
                    values = listOf(10f, 20f, 42f),
                ),
            ),
        )

        composeRule.setContent {
            CPUMonitorTheme(themeMode = com.cpumonitor.core.designsystem.theme.AppThemeMode.Dark) {
                DashboardContent(
                    widgets = widgets,
                    onNavigateToStorage = {},
                    onNavigateToHistory = {},
                    onNavigateToExport = {},
                    onNavigateToProcess = {},
                    onNavigateToAlerts = {},
                    onNavigateToOverlay = {},
                    onNavigateToBenchmark = {},
                    onNavigateToAnalytics = {},
                    onNavigateToSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("CPU Usage").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsDisplayed()
    }
}
