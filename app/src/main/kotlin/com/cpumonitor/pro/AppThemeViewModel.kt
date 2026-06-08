package com.cpumonitor.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.designsystem.theme.AppThemeMode
import com.cpumonitor.core.ui.theme.toThemeMode
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.usecase.settings.ObserveAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val observeAppSettingsUseCase: ObserveAppSettingsUseCase,
) : ViewModel() {

    private val _themeMode = MutableStateFlow<AppThemeMode?>(null)
    val themeMode: StateFlow<AppThemeMode?> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            observeAppSettingsUseCase().collect { result ->
                if (result is Result.Success) {
                    _themeMode.value = result.data.theme.toThemeMode()
                }
            }
        }
    }
}
