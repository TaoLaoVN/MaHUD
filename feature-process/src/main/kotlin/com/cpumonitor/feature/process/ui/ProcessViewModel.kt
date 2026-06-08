package com.cpumonitor.feature.process.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.process.ProcessSortOrder
import com.cpumonitor.domain.model.process.RunningProcess
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.process.ObserveRunningProcessesParams
import com.cpumonitor.domain.usecase.process.ObserveRunningProcessesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessItemUi(
    val pid: Int,
    val name: String,
    val cpuUsagePercent: Float,
    val memoryPssKb: Int,
)

data class ProcessUiData(
    val processes: List<ProcessItemUi>,
    val sortOrder: ProcessSortOrder,
    val searchQuery: String,
)

@HiltViewModel
class ProcessViewModel @Inject constructor(
    private val observeRunningProcessesUseCase: ObserveRunningProcessesUseCase,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS

    private val _sortOrder = MutableStateFlow(ProcessSortOrder.CPU)
    val sortOrder: StateFlow<ProcessSortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<ProcessUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProcessUiData>> = _uiState.asStateFlow()

    private var latestProcesses: List<RunningProcess> = emptyList()
    private var observeJob: Job? = null

    init {
        restartMonitoring()
    }

    fun setSortOrder(sortOrder: ProcessSortOrder) {
        if (_sortOrder.value == sortOrder) return
        _sortOrder.value = sortOrder
        restartMonitoring()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refreshFilteredState()
    }

    private fun restartMonitoring() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeRunningProcessesUseCase(
                ObserveRunningProcessesParams(
                    intervalMs = intervalMs,
                    sortOrder = _sortOrder.value,
                ),
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        latestProcesses = result.data
                        refreshFilteredState()
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "Process monitoring failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun refreshFilteredState() {
        val query = _searchQuery.value.trim().lowercase()
        val filtered = latestProcesses
            .filter { process ->
                query.isEmpty() ||
                    process.processName.lowercase().contains(query) ||
                    process.pid.toString().contains(query)
            }
            .map { process ->
                ProcessItemUi(
                    pid = process.pid,
                    name = process.processName,
                    cpuUsagePercent = process.cpuUsagePercent,
                    memoryPssKb = process.memoryPssKb,
                )
            }

        _uiState.value = UiState.Success(
            ProcessUiData(
                processes = filtered,
                sortOrder = _sortOrder.value,
                searchQuery = _searchQuery.value,
            ),
        )
    }
}
