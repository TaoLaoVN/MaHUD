package com.cpumonitor.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.dashboard.ui.DashboardScreen

const val DashboardRoute = "dashboard"

fun NavGraphBuilder.dashboardScreen(
    onNavigateToStorage: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToProcess: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToOverlay: () -> Unit = {},
    onNavigateToBenchmark: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    composable(route = DashboardRoute) {
        DashboardScreen(
            onNavigateToStorage = onNavigateToStorage,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToExport = onNavigateToExport,
            onNavigateToProcess = onNavigateToProcess,
            onNavigateToAlerts = onNavigateToAlerts,
            onNavigateToOverlay = onNavigateToOverlay,
            onNavigateToBenchmark = onNavigateToBenchmark,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}
