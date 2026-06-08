package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.Result

internal fun <T> unwrap(result: Result<T>): T =
    when (result) {
        is Result.Success -> result.data
        is Result.Error -> throw result.exception
        Result.Loading -> error("Unexpected loading state in monitoring stream")
    }
