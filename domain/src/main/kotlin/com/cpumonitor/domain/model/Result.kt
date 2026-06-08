package com.cpumonitor.domain.model

/**
 * Represents the outcome of a domain operation.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: DomainException) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Base exception type for domain-layer failures.
 */
open class DomainException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
