package com.cpumonitor.feature.export.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.export.ExportFormat
import com.cpumonitor.domain.model.export.ExportedReport
import com.cpumonitor.domain.model.history.HistoryTimeRange
import com.cpumonitor.domain.usecase.export.ExportMetricsReportParams
import com.cpumonitor.domain.usecase.export.ExportMetricsReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val selectedTimeRange: HistoryTimeRange = HistoryTimeRange.TWENTY_FOUR_HOURS,
    val isExporting: Boolean = false,
    val lastExport: ExportedReport? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportMetricsReportUseCase: ExportMetricsReportUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun selectFormat(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format, errorMessage = null) }
    }

    fun selectTimeRange(range: HistoryTimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range, errorMessage = null) }
    }

    fun exportReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            val params = ExportMetricsReportParams(
                format = _uiState.value.selectedFormat,
                timeRange = _uiState.value.selectedTimeRange,
            )
            when (val result = exportMetricsReportUseCase(params)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            lastExport = result.data,
                            errorMessage = null,
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = result.exception.message,
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }
}
