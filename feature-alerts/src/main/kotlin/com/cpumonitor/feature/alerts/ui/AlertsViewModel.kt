package com.cpumonitor.feature.alerts.ui

import androidx.lifecycle.viewModelScope
import com.cpumonitor.core.ui.viewmodel.BaseViewModel
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.usecase.alert.EnsureDefaultAlertRulesUseCase
import com.cpumonitor.domain.usecase.alert.ObserveAlertHistoryParams
import com.cpumonitor.domain.usecase.alert.ObserveAlertHistoryUseCase
import com.cpumonitor.domain.usecase.alert.ObserveAlertRulesUseCase
import com.cpumonitor.domain.usecase.alert.ToggleAlertRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val rules: List<AlertRule> = emptyList(),
    val history: List<AlertHistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val observeAlertRulesUseCase: ObserveAlertRulesUseCase,
    private val observeAlertHistoryUseCase: ObserveAlertHistoryUseCase,
    private val toggleAlertRuleUseCase: ToggleAlertRuleUseCase,
    private val ensureDefaultAlertRulesUseCase: EnsureDefaultAlertRulesUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ensureDefaultAlertRulesUseCase(Unit)
            combine(
                observeAlertRulesUseCase(),
                observeAlertHistoryUseCase(ObserveAlertHistoryParams(limit = 50)),
            ) { rulesResult, historyResult ->
                rulesResult to historyResult
            }.collect { (rulesResult, historyResult) ->
                val error = listOf(rulesResult, historyResult)
                    .filterIsInstance<Result.Error>()
                    .firstOrNull()
                    ?.exception
                    ?.message

                if (error != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    return@collect
                }

                val rules = (rulesResult as Result.Success).data
                val history = (historyResult as Result.Success).data

                _uiState.update {
                    it.copy(
                        rules = rules,
                        history = history,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    fun toggleRule(rule: AlertRule) {
        viewModelScope.launch {
            when (val result = toggleAlertRuleUseCase(rule)) {
                is Result.Error -> {
                    _uiState.update { it.copy(errorMessage = result.exception.message) }
                }
                else -> Unit
            }
        }
    }
}
