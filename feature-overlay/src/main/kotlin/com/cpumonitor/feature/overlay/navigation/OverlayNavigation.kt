package com.cpumonitor.feature.overlay.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.overlay.ui.OverlayScreen

const val OverlayRoute = "overlay"

fun NavGraphBuilder.overlayScreen() {
    composable(route = OverlayRoute) {
        OverlayScreen()
    }
}
