package com.cpumonitor.feature.storage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpumonitor.core.ui.R
import com.cpumonitor.core.ui.localized.localizedName
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cpumonitor.core.designsystem.component.MonitorCard
import com.cpumonitor.core.designsystem.component.MonitorCardHeader
import com.cpumonitor.core.designsystem.component.MonitorMetricValue
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.state.UiState

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            is UiState.Error -> Text(
                text = state.message,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.error,
            )

            is UiState.Success -> StorageContent(
                data = state.data,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun StorageContent(
    data: StorageUiData,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MonitorDimens.spacingLg + contentPadding.calculateLeftPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            end = MonitorDimens.spacingLg + contentPadding.calculateRightPadding(
                androidx.compose.ui.unit.LayoutDirection.Ltr,
            ),
            top = MonitorDimens.spacingLg + contentPadding.calculateTopPadding(),
            bottom = MonitorDimens.spacingLg + contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
    ) {
        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.storage_internal),
                    subtitle = stringResource(R.string.subtitle_realtime),
                )
                MonitorMetricValue(
                    modifier = Modifier.padding(top = MonitorDimens.spacingMd),
                    label = stringResource(R.string.label_used),
                    value = data.usedPercent.toPercentDisplay(),
                    unit = "%",
                    valueColor = MonitorColors.StorageUsage,
                )
                LinearProgressIndicator(
                    progress = { (data.usedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingSm),
                    color = MonitorColors.StorageUsage,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.storage_used_fmt, formatBytes(data.usedBytes)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(R.string.storage_free_fmt, formatBytes(data.freeBytes)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = stringResource(R.string.storage_total_fmt, formatBytes(data.totalBytes)),
                    modifier = Modifier.padding(top = MonitorDimens.spacingXs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.storage_categories),
                    subtitle = stringResource(R.string.storage_groups, data.categories.size),
                )
            }
        }

        items(data.categories, key = { it.category.name }) { category ->
            MonitorCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = category.category.localizedName(), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = formatBytes(category.usedBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonitorColors.StorageUsage,
                    )
                }
                LinearProgressIndicator(
                    progress = { (category.usedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingSm),
                    color = MonitorColors.StorageUsage,
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1_024) return "$bytes B"
    val kb = bytes / 1_024.0
    if (kb < 1_024) return "%.1f KB".format(kb)
    val mb = kb / 1_024.0
    if (mb < 1_024) return "%.1f MB".format(mb)
    val gb = mb / 1_024.0
    return "%.2f GB".format(gb)
}
