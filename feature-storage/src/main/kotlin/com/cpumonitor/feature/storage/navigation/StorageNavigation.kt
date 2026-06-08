package com.cpumonitor.feature.storage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cpumonitor.feature.storage.ui.StorageScreen

const val StorageRoute = "storage"

fun NavGraphBuilder.storageScreen() {
    composable(route = StorageRoute) {
        StorageScreen()
    }
}
