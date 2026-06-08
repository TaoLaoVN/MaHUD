package com.cpumonitor.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R

data class ModuleNavItem(
    val route: String,
    @StringRes val labelRes: Int,
)

val DefaultModuleNavItems: List<ModuleNavItem> = listOf(
    ModuleNavItem(ModuleRoutes.DASHBOARD, R.string.nav_dashboard),
    ModuleNavItem(ModuleRoutes.DEVICE_INFO, R.string.nav_device_info),
    ModuleNavItem(ModuleRoutes.STORAGE, R.string.nav_storage),
    ModuleNavItem(ModuleRoutes.HISTORY, R.string.nav_history),
    ModuleNavItem(ModuleRoutes.EXPORT, R.string.nav_export),
    ModuleNavItem(ModuleRoutes.PROCESS, R.string.nav_process),
    ModuleNavItem(ModuleRoutes.ALERTS, R.string.nav_alerts),
    ModuleNavItem(ModuleRoutes.OVERLAY, R.string.nav_overlay),
    ModuleNavItem(ModuleRoutes.BENCHMARK, R.string.nav_benchmark),
    ModuleNavItem(ModuleRoutes.ANALYTICS, R.string.nav_analytics),
)

object ModuleRoutes {
    const val DASHBOARD = "dashboard"
    const val STORAGE = "storage"
    const val HISTORY = "history"
    const val EXPORT = "export"
    const val PROCESS = "process"
    const val ALERTS = "alerts"
    const val OVERLAY = "overlay"
    const val BENCHMARK = "benchmark"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val DEVICE_INFO = "deviceinfo"
}

@Composable
fun ModuleQuickNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<ModuleNavItem> = DefaultModuleNavItems,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = MonitorDimens.cardElevation,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MonitorDimens.spacingMd,
                    vertical = MonitorDimens.spacingSm,
                ),
        ) {
            items(items, key = { it.route }) { item ->
                val selected = currentRoute == item.route
                AssistChip(
                    onClick = { onNavigate(item.route) },
                    label = { androidx.compose.material3.Text(stringResource(item.labelRes)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                    border = null,
                    modifier = Modifier.padding(end = MonitorDimens.spacingSm),
                )
            }
        }
    }
}
