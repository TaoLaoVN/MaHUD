package com.cpumonitor.pro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.layout.MonitorWindowSizeProvider
import com.cpumonitor.core.designsystem.theme.CPUMonitorTheme
import com.cpumonitor.pro.navigation.CPUMonitorNavHost

@Composable
fun CPUMonitorApp(
    themeViewModel: AppThemeViewModel = hiltViewModel(),
) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

    CPUMonitorTheme(themeMode = themeMode) {
        MonitorWindowSizeProvider {
            CPUMonitorNavHost()
        }
    }
}
