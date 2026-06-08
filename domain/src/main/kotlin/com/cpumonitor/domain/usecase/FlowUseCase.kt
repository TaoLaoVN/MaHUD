package com.cpumonitor.domain.usecase

import com.cpumonitor.domain.model.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Base class for use cases that expose a reactive [Flow] stream.
 *
 * @param Params Input parameters. Use [Unit] when no input is required.
 * @param Type   Emitted value type wrapped in [Result].
 */
abstract class FlowUseCase<in Params, Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected abstract fun execute(params: Params): Flow<Type>

    operator fun invoke(params: Params): Flow<Result<Type>> =
        execute(params)
            .map<Type, Result<Type>> { Result.Success(it) }
            .catch { emit(Result.Error(mapException(it))) }
            .flowOn(dispatcher)

    protected open fun mapException(throwable: Throwable) =
        com.cpumonitor.domain.model.DomainException(
            message = throwable.message ?: "Unknown domain error",
            cause = throwable,
        )
}

/**
 * Convenience base for parameterless flow use cases.
 */
abstract class NoParamsFlowUseCase<Type>(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FlowUseCase<Unit, Type>(dispatcher) {

    operator fun invoke(): Flow<Result<Type>> = invoke(Unit)

    final override fun execute(params: Unit): Flow<Type> = execute()
    protected abstract fun execute(): Flow<Type>
}
