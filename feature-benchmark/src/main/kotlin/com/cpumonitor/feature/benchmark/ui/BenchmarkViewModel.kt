package com.cpumonitor.feature.benchmark.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.BenchmarkProgress
import com.cpumonitor.domain.model.benchmark.BenchmarkResult
import com.cpumonitor.domain.model.benchmark.BenchmarkSessionStatus
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.benchmark.StressTestResult
import com.cpumonitor.domain.usecase.benchmark.CancelBenchmarkSessionUseCase
import com.cpumonitor.domain.usecase.benchmark.ObserveBenchmarkProgressUseCase
import com.cpumonitor.domain.usecase.benchmark.RunCpuBenchmarkUseCase
import com.cpumonitor.domain.usecase.benchmark.RunStressTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BenchmarkUiState(
    val progress: BenchmarkProgress = BenchmarkProgress.idle(),
    val selectedMode: BenchmarkMode = BenchmarkMode.MULTI_CORE,
    val selectedStressDuration: StressTestDuration = StressTestDuration.MINUTES_5,
    val lastBenchmarkResult: BenchmarkResult? = null,
    val lastStressResult: StressTestResult? = null,
    val errorMessage: String? = null,
    val isRunning: Boolean = false,
)

@HiltViewModel
class BenchmarkViewModel @Inject constructor(
    private val observeBenchmarkProgressUseCase: ObserveBenchmarkProgressUseCase,
    private val runCpuBenchmarkUseCase: RunCpuBenchmarkUseCase,
    private val runStressTestUseCase: RunStressTestUseCase,
    private val cancelBenchmarkSessionUseCase: CancelBenchmarkSessionUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    init {
        progressJob = viewModelScope.launch {
            observeBenchmarkProgressUseCase(Unit).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val progress = result.data
                        _uiState.update {
                            it.copy(
                                progress = progress,
                                isRunning = progress.status == BenchmarkSessionStatus.RUNNING,
                                errorMessage = if (progress.status == BenchmarkSessionStatus.FAILED) {
                                    progress.message
                                } else {
                                    null
                                },
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(errorMessage = result.exception.message)
                        }
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    fun selectMode(mode: BenchmarkMode) {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun selectStressDuration(duration: StressTestDuration) {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(selectedStressDuration = duration) }
    }

    fun runBenchmark() {
        if (_uiState.value.isRunning) return
        val mode = _uiState.value.selectedMode

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = runCpuBenchmarkUseCase(mode)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(lastBenchmarkResult = result.data, errorMessage = null)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun runStressTest() {
        if (_uiState.value.isRunning) return
        val duration = _uiState.value.selectedStressDuration

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = runStressTestUseCase(duration)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(lastStressResult = result.data, errorMessage = null)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun cancelSession() {
        viewModelScope.launch {
            when (val result = cancelBenchmarkSessionUseCase()) {
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                else -> Unit
            }
        }
    }
}
