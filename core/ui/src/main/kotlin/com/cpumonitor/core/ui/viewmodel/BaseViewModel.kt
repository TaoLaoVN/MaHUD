package com.cpumonitor.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.cpumonitor.domain.model.Result

/**
 * Base ViewModel with helpers to map domain [Result] to UI state.
 * ViewModels must not contain business logic — delegate to use cases.
 */
abstract class BaseViewModel : ViewModel() {

    protected fun <T> Result<T>.toUiMessage(): String? = when (this) {
        is Result.Error -> exception.message
        else -> null
    }
}
