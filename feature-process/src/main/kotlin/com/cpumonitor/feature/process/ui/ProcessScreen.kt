package com.cpumonitor.feature.process.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.cpumonitor.core.designsystem.model.toPercentDisplay
import com.cpumonitor.core.designsystem.theme.MonitorColors
import com.cpumonitor.core.designsystem.theme.MonitorDimens
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.domain.model.process.ProcessSortOrder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessScreen(
    viewModel: ProcessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

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

            is UiState.Success -> ProcessContent(
                data = state.data,
                sortOrder = sortOrder,
                searchQuery = searchQuery,
                onSortOrderSelected = viewModel::setSortOrder,
                onSearchQueryChanged = viewModel::setSearchQuery,
                contentPadding = padding,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProcessContent(
    data: ProcessUiData,
    sortOrder: ProcessSortOrder,
    searchQuery: String,
    onSortOrderSelected: (ProcessSortOrder) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(MonitorDimens.spacingMd),
    ) {
        item {
            MonitorCard {
                MonitorCardHeader(
                    title = stringResource(R.string.process_search),
                    subtitle = stringResource(R.string.process_search_subtitle),
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.process_search_hint)) },
                )
            }
        }

        item {
            MonitorCard {
                MonitorCardHeader(title = stringResource(R.string.process_sort))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MonitorDimens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(MonitorDimens.spacingSm),
                ) {
                    ProcessSortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { onSortOrderSelected(order) },
                            label = { Text(order.localizedName()) },
                        )
                    }
                }
            }
        }

        if (data.processes.isEmpty()) {
            item {
                MonitorCard {
                    MonitorCardHeader(
                        title = stringResource(R.string.process_none),
                        subtitle = stringResource(R.string.process_none_subtitle),
                    )
                }
            }
        } else {
            items(data.processes, key = { it.pid }) { process ->
                MonitorCard {
                    MonitorCardHeader(
                        title = process.name,
                        subtitle = stringResource(R.string.process_pid, process.pid),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MonitorDimens.spacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.process_cpu_fmt,
                                process.cpuUsagePercent.toPercentDisplay(),
                            ),
                            color = MonitorColors.CpuUsage,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.process_ram_fmt, process.memoryPssKb),
                            color = MonitorColors.MemoryUsage,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
