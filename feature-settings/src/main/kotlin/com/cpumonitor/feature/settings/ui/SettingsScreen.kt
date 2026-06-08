package com.cpumonitor.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.layout.MonitorWindowSizeProvider
import com.cpumonitor.core.designsystem.layout.horizontalContentPadding
import com.cpumonitor.core.designsystem.layout.rememberMonitorWindowSize
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedName
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPeriod
import com.cpumonitor.domain.model.settings.RetentionPolicy
import com.cpumonitor.domain.performance.PerformanceBudget
import com.cpumonitor.feature.settings.permission.BatteryOptimizationEffect
import com.cpumonitor.feature.settings.update.AppUpdateInstaller
import java.io.File
import com.cpumonitor.feature.settings.permission.isBatteryOptimizationIgnored
import com.cpumonitor.feature.settings.permission.openBatteryOptimizationSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MonitorWindowSizeProvider {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.settings)) })
            },
        ) { padding ->
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )

                else -> SettingsContent(
                    uiState = uiState,
                    contentPadding = padding,
                    onThemeSelected = viewModel::onThemeSelected,
                    onBackgroundRefreshChanged = viewModel::onBackgroundRefreshChanged,
                    onRetentionChanged = viewModel::onRetentionChanged,
                    onCheckForUpdate = viewModel::checkForUpdate,
                    onDownloadUpdate = viewModel::downloadUpdate,
                    downloadedApkPath = viewModel::downloadedApkPath,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    contentPadding: PaddingValues,
    onThemeSelected: (AppTheme) -> Unit,
    onBackgroundRefreshChanged: (Int) -> Unit,
    onRetentionChanged: (RetentionMetricKey, RetentionPeriod) -> Unit,
    onCheckForUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    downloadedApkPath: () -> String?,
) {
    val windowSize = rememberMonitorWindowSize()
    val horizontalPadding = windowSize.horizontalContentPadding()
    val context = LocalContext.current
    val onInstallUpdate: () -> Unit = {
        downloadedApkPath()?.let { path ->
            val apkFile = File(path)
            if (apkFile.exists()) {
                if (AppUpdateInstaller.canInstallPackages(context)) {
                    AppUpdateInstaller.installApk(context, apkFile)
                } else {
                    AppUpdateInstaller.openInstallPermissionSettings(context)
                }
            }
        }
    }
    var batteryExempt by remember { mutableStateOf(isBatteryOptimizationIgnored(context)) }

    BatteryOptimizationEffect(onStatusChanged = { batteryExempt = it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = horizontalPadding + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingSm + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingMd + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingXs),
    ) {
        uiState.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        item {
            AppUpdateCard(
                appUpdate = uiState.appUpdate,
                onCheckForUpdate = onCheckForUpdate,
                onDownloadUpdate = onDownloadUpdate,
                onInstallUpdate = onInstallUpdate,
            )
        }

        item {
            BackgroundMonitoringCard(
                batteryExempt = batteryExempt,
                intervalSeconds = uiState.settings.backgroundRefreshIntervalSeconds,
                onIntervalChanged = onBackgroundRefreshChanged,
                onRequestBatteryExemption = { openBatteryOptimizationSettings(context) },
            )
        }

        item {
            ThemeCard(
                selectedTheme = uiState.settings.theme,
                onThemeSelected = onThemeSelected,
            )
        }

        item {
            RetentionCard(
                policy = uiState.settings.retentionPolicy,
                onRetentionChanged = onRetentionChanged,
            )
        }

        item {
            PerformanceCard(performance = uiState.performance)
        }
    }
}

@Composable
private fun AppUpdateCard(
    appUpdate: AppUpdateUiState,
    onCheckForUpdate: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    MonitorCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.settings_update_title),
                subtitle = stringResource(
                    R.string.settings_update_current_version,
                    appUpdate.currentVersionName,
                ),
            )

            when (appUpdate.phase) {
                AppUpdatePhase.CHECKING -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Text(
                            text = stringResource(R.string.settings_update_checking),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                AppUpdatePhase.UP_TO_DATE -> {
                    Text(
                        text = stringResource(R.string.settings_update_up_to_date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                AppUpdatePhase.UPDATE_AVAILABLE -> {
                    val release = appUpdate.release
                    Text(
                        text = stringResource(
                            R.string.settings_update_available,
                            release?.versionName.orEmpty(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    release?.releaseNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Text(
                            text = notes.take(280).let { preview ->
                                if (notes.length > 280) "$preview…" else preview
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = onDownloadUpdate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_update_download))
                    }
                }

                AppUpdatePhase.DOWNLOADING -> {
                    Text(
                        text = stringResource(R.string.settings_update_downloading),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LinearProgressIndicator(
                        progress = { appUpdate.downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${(appUpdate.downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                AppUpdatePhase.READY_TO_INSTALL -> {
                    Text(
                        text = stringResource(R.string.settings_update_ready),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Button(
                        onClick = onInstallUpdate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_update_install))
                    }
                }

                AppUpdatePhase.ERROR -> {
                    Text(
                        text = appUpdate.message ?: stringResource(R.string.settings_update_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                AppUpdatePhase.IDLE -> Unit
            }

            if (appUpdate.phase != AppUpdatePhase.CHECKING &&
                appUpdate.phase != AppUpdatePhase.DOWNLOADING
            ) {
                TextButton(
                    onClick = onCheckForUpdate,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.settings_update_check_again))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackgroundMonitoringCard(
    batteryExempt: Boolean,
    intervalSeconds: Int,
    onIntervalChanged: (Int) -> Unit,
    onRequestBatteryExemption: () -> Unit,
) {
    MonitorCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.settings_background_title),
                subtitle = stringResource(R.string.settings_background_subtitle),
            )
            Text(
                text = if (batteryExempt) {
                    stringResource(R.string.settings_battery_exempt)
                } else {
                    stringResource(R.string.settings_battery_restricted)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (batteryExempt) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            if (!batteryExempt) {
                Button(
                    onClick = onRequestBatteryExemption,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.settings_battery_allow))
                }
            }
            Text(
                text = stringResource(R.string.settings_refresh_interval, intervalSeconds),
                style = MaterialTheme.typography.labelLarge,
            )
            Slider(
                value = intervalSeconds.toFloat(),
                onValueChange = { onIntervalChanged(it.toInt()) },
                valueRange = PerformanceBudget.MIN_BACKGROUND_REFRESH_SECONDS.toFloat()..
                    PerformanceBudget.MAX_BACKGROUND_REFRESH_SECONDS.toFloat(),
                steps = PerformanceBudget.MAX_BACKGROUND_REFRESH_SECONDS -
                    PerformanceBudget.MIN_BACKGROUND_REFRESH_SECONDS - 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeCard(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    MonitorCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.theme_title),
                subtitle = stringResource(R.string.theme_subtitle),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
            ) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = selectedTheme == theme,
                        onClick = { onThemeSelected(theme) },
                        label = { Text(theme.localizedName()) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RetentionCard(
    policy: RetentionPolicy,
    onRetentionChanged: (RetentionMetricKey, RetentionPeriod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    MonitorCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.data_retention),
                subtitle = stringResource(
                    R.string.settings_retention_summary,
                    policy.cpuRetention.localizedName(),
                    policy.memoryRetention.localizedName(),
                    policy.thermalRetention.localizedName(),
                    policy.batteryRetention.localizedName(),
                ),
            )
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = if (expanded) {
                        stringResource(R.string.settings_retention_collapse)
                    } else {
                        stringResource(R.string.settings_retention_expand)
                    },
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm)) {
                    CompactRetentionRow(stringResource(R.string.metric_cpu), policy.cpuRetention) {
                        onRetentionChanged(RetentionMetricKey.CPU, it)
                    }
                    CompactRetentionRow(stringResource(R.string.metric_memory), policy.memoryRetention) {
                        onRetentionChanged(RetentionMetricKey.MEMORY, it)
                    }
                    CompactRetentionRow(stringResource(R.string.metric_thermal), policy.thermalRetention) {
                        onRetentionChanged(RetentionMetricKey.THERMAL, it)
                    }
                    CompactRetentionRow(stringResource(R.string.metric_battery), policy.batteryRetention) {
                        onRetentionChanged(RetentionMetricKey.BATTERY, it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactRetentionRow(
    label: String,
    selected: RetentionPeriod,
    onSelected: (RetentionPeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(0.28f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            modifier = Modifier.weight(0.72f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RetentionPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selected == period,
                    onClick = { onSelected(period) },
                    label = {
                        Text(
                            text = period.localizedName(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PerformanceCard(performance: PerformanceUiState) {
    MonitorCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
        ) {
            MonitorCardHeader(
                title = stringResource(R.string.performance),
                subtitle = stringResource(R.string.performance_subtitle),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CompactPerformanceStat(
                    label = stringResource(R.string.cpu_overhead),
                    value = "%.2f%%".format(performance.cpuOverheadPercent),
                    withinBudget = performance.cpuWithinBudget,
                )
                CompactPerformanceStat(
                    label = stringResource(R.string.memory_used),
                    value = performance.memoryUsedMb,
                    withinBudget = performance.memoryWithinBudget,
                )
            }
        }
    }
}

@Composable
private fun CompactPerformanceStat(
    label: String,
    value: String,
    withinBudget: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (withinBudget) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
    }
}
