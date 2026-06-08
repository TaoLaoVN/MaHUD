package com.cpumonitor.core.ui.theme

import com.cpumonitor.core.designsystem.theme.AppThemeMode
import com.cpumonitor.domain.model.settings.AppTheme

/** Maps persisted [AppTheme] to Compose [AppThemeMode]. */
fun AppTheme.toThemeMode(): AppThemeMode = when (this) {
    AppTheme.LIGHT -> AppThemeMode.Light
    AppTheme.DARK -> AppThemeMode.Dark
    AppTheme.AMOLED -> AppThemeMode.Amoled
}
