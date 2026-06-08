package com.cpumonitor.feature.settings.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.monitoring.AppMemorySnapshot
import com.cpumonitor.core.monitoring.MonitoringOverheadTracker
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPeriod
import com.cpumonitor.domain.model.settings.RetentionPolicy
import com.cpumonitor.domain.performance.PerformanceBudget
import com.cpumonitor.domain.usecase.settings.ObserveAppSettingsUseCase
import com.cpumonitor.domain.usecase.settings.UpdateBackgroundRefreshIntervalUseCase
import com.cpumonitor.domain.usecase.settings.UpdateRetentionPolicyUseCase
import com.cpumonitor.domain.model.update.AppUpdateStatus
import com.cpumonitor.domain.provider.AppVersionProvider
import com.cpumonitor.domain.usecase.settings.UpdateThemeUseCase
import com.cpumonitor.domain.usecase.update.CheckForAppUpdateUseCase
import com.cpumonitor.domain.usecase.update.DownloadAppUpdateParams
import com.cpumonitor.domain.usecase.update.DownloadAppUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceUiState(
    val cpuOverheadPercent: Float = 0f,
    val memoryUsedMb: String = "0.0 MB",
    val memoryWithinBudget: Boolean = true,
    val cpuWithinBudget: Boolean = true,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val performance: PerformanceUiState = PerformanceUiState(),
    val appUpdate: AppUpdateUiState = AppUpdateUiState(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateBackgroundRefreshIntervalUseCase: UpdateBackgroundRefreshIntervalUseCase,
    private val updateRetentionPolicyUseCase: UpdateRetentionPolicyUseCase,
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase,
    private val downloadAppUpdateUseCase: DownloadAppUpdateUseCase,
    appVersionProvider: AppVersionProvider,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appUpdate = AppUpdateUiState(
                currentVersionName = appVersionProvider.versionName,
            ),
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var downloadedApkPath: String? = null

    init {
        checkForUpdate()
        viewModelScope.launch {
            observeAppSettingsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val overhead = MonitoringOverheadTracker.snapshot()
                        val memory = AppMemorySnapshot.current()
                        _uiState.update {
                            it.copy(
                                settings = result.data,
                                performance = PerformanceUiState(
                                    cpuOverheadPercent = overhead.overheadPercent,
                                    memoryUsedMb = AppMemorySnapshot.formatMegabytes(memory.usedBytes),
                                    memoryWithinBudget = memory.withinBudget,
                                    cpuWithinBudget = overhead.withinBudget,
                                ),
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }

                    is Result.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message,
                        )
                    }

                    is Result.Loading -> Unit
                }
            }
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        if (_uiState.value.settings.theme == theme) return
        viewModelScope.launch {
            val result = updateThemeUseCase(theme)
            if (result is Result.Error) {
                _uiState.update { it.copy(errorMessage = result.exception.message) }
            }
        }
    }

    fun onBackgroundRefreshChanged(seconds: Int) {
        val clamped = seconds.coerceIn(
            PerformanceBudget.MIN_BACKGROUND_REFRESH_SECONDS,
            PerformanceBudget.MAX_BACKGROUND_REFRESH_SECONDS,
        )
        viewModelScope.launch {
            val result = updateBackgroundRefreshIntervalUseCase(clamped)
            if (result is Result.Error) {
                _uiState.update { it.copy(errorMessage = result.exception.message) }
            }
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    appUpdate = it.appUpdate.copy(
                        phase = AppUpdatePhase.CHECKING,
                        message = null,
                    ),
                )
            }
            when (val result = checkForAppUpdateUseCase()) {
                is Result.Success -> {
                    val phase = when (val status = result.data) {
                        AppUpdateStatus.UpToDate -> AppUpdatePhase.UP_TO_DATE
                        is AppUpdateStatus.UpdateAvailable -> AppUpdatePhase.UPDATE_AVAILABLE
                        is AppUpdateStatus.NoReleaseFound -> AppUpdatePhase.ERROR
                    }
                    _uiState.update {
                        it.copy(
                            appUpdate = it.appUpdate.copy(
                                phase = phase,
                                release = (result.data as? AppUpdateStatus.UpdateAvailable)?.release,
                                message = (result.data as? AppUpdateStatus.NoReleaseFound)?.message,
                            ),
                        )
                    }
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        appUpdate = it.appUpdate.copy(
                            phase = AppUpdatePhase.ERROR,
                            message = result.exception.message,
                        ),
                    )
                }

                is Result.Loading -> Unit
            }
        }
    }

    fun downloadUpdate() {
        val release = _uiState.value.appUpdate.release ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    appUpdate = it.appUpdate.copy(
                        phase = AppUpdatePhase.DOWNLOADING,
                        downloadProgress = 0f,
                        message = null,
                    ),
                )
            }
            val result = downloadAppUpdateUseCase(
                DownloadAppUpdateParams(
                    downloadUrl = release.apkDownloadUrl,
                    versionName = release.versionName,
                    onProgress = { progress ->
                        _uiState.update {
                            it.copy(
                                appUpdate = it.appUpdate.copy(downloadProgress = progress),
                            )
                        }
                    },
                ),
            )
            when (result) {
                is Result.Success -> {
                    downloadedApkPath = result.data.filePath
                    _uiState.update {
                        it.copy(
                            appUpdate = it.appUpdate.copy(
                                phase = AppUpdatePhase.READY_TO_INSTALL,
                                downloadProgress = 1f,
                            ),
                        )
                    }
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        appUpdate = it.appUpdate.copy(
                            phase = AppUpdatePhase.ERROR,
                            message = result.exception.message,
                        ),
                    )
                }

                is Result.Loading -> Unit
            }
        }
    }

    fun downloadedApkPath(): String? = downloadedApkPath

    fun onRetentionChanged(metric: RetentionMetricKey, period: RetentionPeriod) {
        val current = _uiState.value.settings.retentionPolicy
        val updated = when (metric) {
            RetentionMetricKey.CPU -> current.copy(cpuRetention = period)
            RetentionMetricKey.MEMORY -> current.copy(memoryRetention = period)
            RetentionMetricKey.THERMAL -> current.copy(thermalRetention = period)
            RetentionMetricKey.BATTERY -> current.copy(batteryRetention = period)
        }
        viewModelScope.launch {
            val result = updateRetentionPolicyUseCase(updated)
            if (result is Result.Error) {
                _uiState.update { it.copy(errorMessage = result.exception.message) }
            }
        }
    }
}

enum class RetentionMetricKey {
    CPU,
    MEMORY,
    THERMAL,
    BATTERY,
}
