package com.cpumonitor.data.repository

import com.cpumonitor.domain.model.DomainException
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Base repository providing safe execution helpers.
 * All repository implementations must extend this class.
 */
abstract class BaseRepository(
    protected val dispatcher: CoroutineDispatcher,
) : Repository {

    protected suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(dispatcher) {
            try {
                Result.Success(block())
            } catch (exception: Exception) {
                Result.Error(
                    DomainException(
                        message = exception.message ?: "Repository operation failed",
                        cause = exception,
                    ),
                )
            }
        }

    /**
     * Wraps a realtime [Flow] stream in [Result], routing emissions through [dispatcher].
     */
    protected fun <T> observeSafely(source: () -> Flow<T>): Flow<Result<T>> =
        source()
            .map<T, Result<T>> { Result.Success(it) }
            .catch { throwable ->
                emit(
                    Result.Error(
                        DomainException(
                            message = throwable.message ?: "Repository stream failed",
                            cause = throwable,
                        ),
                    ),
                )
            }
            .flowOn(dispatcher)
}
