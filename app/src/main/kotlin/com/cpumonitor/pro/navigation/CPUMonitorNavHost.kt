package com.cpumonitor.pro.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cpumonitor.core.ui.navigation.ModuleQuickNavBar
import com.cpumonitor.core.ui.navigation.ModuleRoutes
import com.cpumonitor.feature.alerts.navigation.alertsScreen
import com.cpumonitor.feature.analytics.navigation.analyticsScreen
import com.cpumonitor.feature.battery.navigation.batteryScreen
import com.cpumonitor.feature.benchmark.navigation.benchmarkScreen
import com.cpumonitor.feature.cpu.navigation.cpuScreen
import com.cpumonitor.feature.dashboard.navigation.dashboardScreen
import com.cpumonitor.feature.deviceinfo.navigation.deviceinfoScreen
import com.cpumonitor.feature.export.navigation.exportScreen
import com.cpumonitor.feature.history.navigation.historyScreen
import com.cpumonitor.feature.memory.navigation.memoryScreen
import com.cpumonitor.feature.overlay.navigation.overlayScreen
import com.cpumonitor.feature.process.navigation.processScreen
import com.cpumonitor.feature.settings.navigation.settingsScreen
import com.cpumonitor.feature.storage.navigation.storageScreen
import com.cpumonitor.feature.thermal.navigation.thermalScreen

private val moduleBarRoutes = setOf(
    ModuleRoutes.DASHBOARD,
    ModuleRoutes.DEVICE_INFO,
    ModuleRoutes.STORAGE,
    ModuleRoutes.HISTORY,
    ModuleRoutes.EXPORT,
    ModuleRoutes.PROCESS,
    ModuleRoutes.ALERTS,
    ModuleRoutes.OVERLAY,
    ModuleRoutes.BENCHMARK,
    ModuleRoutes.ANALYTICS,
    ModuleRoutes.SETTINGS,
)

@Composable
fun CPUMonitorNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateToModule(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in moduleBarRoutes) {
                ModuleQuickNavBar(
                    currentRoute = currentRoute,
                    onNavigate = ::navigateToModule,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ModuleRoutes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            dashboardScreen(
                onNavigateToStorage = { navigateToModule(ModuleRoutes.STORAGE) },
                onNavigateToHistory = { navigateToModule(ModuleRoutes.HISTORY) },
                onNavigateToExport = { navigateToModule(ModuleRoutes.EXPORT) },
                onNavigateToProcess = { navigateToModule(ModuleRoutes.PROCESS) },
                onNavigateToAlerts = { navigateToModule(ModuleRoutes.ALERTS) },
                onNavigateToOverlay = { navigateToModule(ModuleRoutes.OVERLAY) },
                onNavigateToBenchmark = { navigateToModule(ModuleRoutes.BENCHMARK) },
                onNavigateToAnalytics = { navigateToModule(ModuleRoutes.ANALYTICS) },
                onNavigateToSettings = { navigateToModule(ModuleRoutes.SETTINGS) },
            )
            cpuScreen()
            memoryScreen()
            batteryScreen()
            thermalScreen()
            storageScreen()
            processScreen()
            deviceinfoScreen()
            benchmarkScreen()
            historyScreen()
            exportScreen()
            overlayScreen()
            alertsScreen()
            analyticsScreen()
            settingsScreen()
        }
    }
}
