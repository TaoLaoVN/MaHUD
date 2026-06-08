package com.cpumonitor.feature.deviceinfo.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.state.UiState
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.device.AuthenticityReport
import com.cpumonitor.domain.model.device.DeviceSpec
import com.cpumonitor.domain.usecase.device.GetDeviceSpecUseCase
import com.cpumonitor.domain.usecase.device.ValidateDeviceAuthenticityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceInfoUiData(
    val spec: DeviceSpec,
    val report: AuthenticityReport,
)

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val getDeviceSpecUseCase: GetDeviceSpecUseCase,
    private val validateDeviceAuthenticityUseCase: ValidateDeviceAuthenticityUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<DeviceInfoUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DeviceInfoUiData>> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val specResult = getDeviceSpecUseCase()
            val authenticityResult = validateDeviceAuthenticityUseCase()

            when {
                specResult is Result.Error -> {
                    _uiState.value = UiState.Error(
                        specResult.exception.message ?: "Failed to read device specifications",
                    )
                }
                authenticityResult is Result.Error -> {
                    _uiState.value = UiState.Error(
                        authenticityResult.exception.message ?: "Failed to validate device authenticity",
                    )
                }
                specResult is Result.Success && authenticityResult is Result.Success -> {
                    _uiState.value = UiState.Success(
                        DeviceInfoUiData(
                            spec = specResult.data,
                            report = authenticityResult.data,
                        ),
                    )
                }
            }
        }
    }
}
