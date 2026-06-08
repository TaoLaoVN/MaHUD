package com.cpumonitor.feature.overlay.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.OverlayConfig
import com.cpumonitor.domain.model.OverlayMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.usecase.overlay.CheckOverlayPermissionUseCase
import com.cpumonitor.domain.usecase.overlay.IsOverlayRunningUseCase
import com.cpumonitor.domain.usecase.overlay.ObserveOverlayMetricsParams
import com.cpumonitor.domain.usecase.overlay.ObserveOverlayMetricsUseCase
import com.cpumonitor.domain.usecase.overlay.StartOverlayMonitoringUseCase
import com.cpumonitor.domain.usecase.overlay.StopOverlayMonitoringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OverlayUiState(
    val isPermissionGranted: Boolean = false,
    val isOverlayRunning: Boolean = false,
    val gamingModeEnabled: Boolean = false,
    val previewMetrics: OverlayMetrics? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val checkOverlayPermissionUseCase: CheckOverlayPermissionUseCase,
    private val startOverlayMonitoringUseCase: StartOverlayMonitoringUseCase,
    private val stopOverlayMonitoringUseCase: StopOverlayMonitoringUseCase,
    private val isOverlayRunningUseCase: IsOverlayRunningUseCase,
    private val observeOverlayMetricsUseCase: ObserveOverlayMetricsUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(OverlayUiState())
    val uiState: StateFlow<OverlayUiState> = _uiState.asStateFlow()

    private var previewJob: Job? = null

    init {
        refreshOverlayState()
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(isPermissionGranted = granted, errorMessage = null) }
    }

    fun setGamingModeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(gamingModeEnabled = enabled) }
        if (!_uiState.value.isOverlayRunning) {
            restartPreview()
        }
    }

    fun startOverlay() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val config = OverlayConfig(gamingModeEnabled = _uiState.value.gamingModeEnabled)
            when (val result = startOverlayMonitoringUseCase(config)) {
                is Result.Success -> {
                    stopPreview()
                    refreshOverlayState()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun stopOverlay() {
        viewModelScope.launch {
            when (val result = stopOverlayMonitoringUseCase()) {
                is Result.Success -> {
                    refreshOverlayState()
                    restartPreview()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun refreshOverlayState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val permissionGranted = when (val permissionResult = checkOverlayPermissionUseCase()) {
                is Result.Success -> permissionResult.data
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = permissionResult.exception.message,
                        )
                    }
                    return@launch
                }
                Result.Loading -> false
            }

            val isRunning = when (val runningResult = isOverlayRunningUseCase()) {
                is Result.Success -> runningResult.data
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = runningResult.exception.message,
                        )
                    }
                    return@launch
                }
                Result.Loading -> false
            }

            _uiState.update {
                it.copy(
                    isPermissionGranted = permissionGranted,
                    isOverlayRunning = isRunning,
                    isLoading = false,
                )
            }

            if (!isRunning) {
                restartPreview()
            } else {
                stopPreview()
            }
        }
    }

    private fun restartPreview() {
        stopPreview()
        val refreshIntervalMs = if (_uiState.value.gamingModeEnabled) {
            OverlayConfig.GAMING_REFRESH_INTERVAL_MS
        } else {
            OverlayConfig.NORMAL_REFRESH_INTERVAL_MS
        }

        previewJob = viewModelScope.launch {
            observeOverlayMetricsUseCase(
                ObserveOverlayMetricsParams(refreshIntervalMs = refreshIntervalMs),
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { state ->
                            state.copy(previewMetrics = result.data, errorMessage = null)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(errorMessage = result.exception.message) }
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        _uiState.update { it.copy(previewMetrics = null) }
    }
}
