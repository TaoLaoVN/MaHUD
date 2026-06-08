package com.cpumonitor.feature.storage.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.StorageCategory
import com.cpumonitor.domain.model.StorageCategoryUsage
import com.cpumonitor.domain.model.StorageMetrics
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.usecase.monitoring.ObserveStorageUsageParams
import com.cpumonitor.domain.usecase.monitoring.ObserveStorageUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class StorageCategoryUi(
    val category: StorageCategory,
    val usedBytes: Long,
    val usedPercent: Float,
)

data class StorageUiData(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercent: Float,
    val categories: List<StorageCategoryUi>,
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val observeStorageUsageUseCase: ObserveStorageUsageUseCase,
) : BaseViewModel() {

    private val intervalMs = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS

    private val _uiState = MutableStateFlow<UiState<StorageUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<StorageUiData>> = _uiState.asStateFlow()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            observeStorageUsageUseCase(ObserveStorageUsageParams(intervalMs)).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.value = UiState.Success(mapToUiData(result.data))
                    is Result.Error -> {
                        _uiState.value = UiState.Error(
                            result.exception.message ?: "Storage monitoring failed",
                        )
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    private fun mapToUiData(metrics: StorageMetrics): StorageUiData {
        val usedPercent = if (metrics.totalBytes <= 0L) {
            0f
        } else {
            ((metrics.usedBytes.toDouble() / metrics.totalBytes.toDouble()) * 100.0)
                .roundToInt()
                .toFloat()
        }

        return StorageUiData(
            totalBytes = metrics.totalBytes,
            usedBytes = metrics.usedBytes,
            freeBytes = metrics.freeBytes,
            usedPercent = usedPercent,
            categories = metrics.categories.map { it.toCategoryUi(metrics.usedBytes) },
        )
    }

    private fun StorageCategoryUsage.toCategoryUi(totalUsedBytes: Long): StorageCategoryUi {
        val percent = if (totalUsedBytes <= 0L) {
            0f
        } else {
            ((usedBytes.toDouble() / totalUsedBytes.toDouble()) * 100.0).roundToInt().toFloat()
        }
        return StorageCategoryUi(
            category = category,
            usedBytes = usedBytes,
            usedPercent = percent,
        )
    }
}
