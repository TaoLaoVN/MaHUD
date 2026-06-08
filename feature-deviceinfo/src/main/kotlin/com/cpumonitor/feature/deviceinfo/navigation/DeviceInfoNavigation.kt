package com.cpumonitor.feature.deviceinfo.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.deviceinfo.ui.DeviceInfoScreen

import com.cpumonitor.core.ui.navigation.ModuleRoutes

const val DeviceInfoRoute = ModuleRoutes.DEVICE_INFO

fun NavGraphBuilder.deviceinfoScreen() {
    composable(route = DeviceInfoRoute) {
        DeviceInfoScreen()
    }
}
