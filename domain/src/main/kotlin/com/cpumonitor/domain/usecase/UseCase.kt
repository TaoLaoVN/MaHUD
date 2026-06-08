package com.cpumonitor.domain.usecase

import com.cpumonitor.domain.model.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for synchronous domain use cases executed on a background dispatcher.
 *
 * @param Params Input parameters. Use [Unit] when no input is required.
 * @param Type   Output type wrapped in [Result].
 */
abstract class UseCase<in Params, out Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected abstract suspend fun execute(params: Params): Result<Type>

    suspend operator fun invoke(params: Params): Result<Type> = withContext(dispatcher) {
        execute(params)
    }
}

/**
 * Convenience base for parameterless use cases.
 */
abstract class NoParamsUseCase<out Type>(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Unit, Type>(dispatcher) {

    suspend operator fun invoke(): Result<Type> = invoke(Unit)

    final override suspend fun execute(params: Unit): Result<Type> = execute()
    protected abstract suspend fun execute(): Result<Type>
}
