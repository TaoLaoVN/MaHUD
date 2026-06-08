package com.cpumonitor.feature.analytics.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.analytics.AnalyticsDashboard
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.analytics.ObserveAnalyticsDashboardParams
import com.cpumonitor.domain.usecase.analytics.ObserveAnalyticsDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val dashboard: AnalyticsDashboard? = null,
    val selectedWindow: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val observeAnalyticsDashboardUseCase: ObserveAnalyticsDashboardUseCase,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeDashboard(HistoryTimeRange.TWENTY_FOUR_HOURS)
    }

    fun selectWindow(window: HistoryTimeRange) {
        if (_uiState.value.selectedWindow == window) return
        observeDashboard(window)
    }

    private fun observeDashboard(window: HistoryTimeRange) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(selectedWindow = window, isLoading = true, errorMessage = null) }

            observeAnalyticsDashboardUseCase(
                ObserveAnalyticsDashboardParams(
                    intervalMs = intervalMs,
                    window = window,
                ),
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                dashboard = result.data,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exception.message,
                            )
                        }
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }
}
