package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.benchmark.BenchmarkCalculators
import com.cpumonitor.data.datasource.benchmark.CpuBenchmarkEngine
import com.cpumonitor.domain.model.DomainException
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.model.benchmark.BenchmarkProgress
import com.cpumonitor.domain.model.benchmark.BenchmarkResult
import com.cpumonitor.domain.model.benchmark.BenchmarkSessionStatus
import com.cpumonitor.domain.model.benchmark.BenchmarkSessionType
import com.cpumonitor.domain.model.benchmark.StressTestDuration
import com.cpumonitor.domain.model.benchmark.StressTestResult
import com.cpumonitor.domain.repository.BenchmarkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BenchmarkRepositoryImpl @Inject constructor(
    private val cpuBenchmarkEngine: CpuBenchmarkEngine,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), BenchmarkRepository {

    private val progressState = MutableStateFlow(BenchmarkProgress.idle())
    private val sessionMutex = Mutex()

    override fun observeProgress(): Flow<BenchmarkProgress> = progressState.asStateFlow()

    override suspend fun runCpuBenchmark(mode: BenchmarkMode): Result<BenchmarkResult> =
        sessionMutex.withLock {
            if (progressState.value.status == BenchmarkSessionStatus.RUNNING) {
                return Result.Error(DomainException("Another benchmark session is already running"))
            }

            withContext(dispatcher) {
                try {
                    updateProgress(
                        status = BenchmarkSessionStatus.RUNNING,
                        sessionType = BenchmarkSessionType.CPU_BENCHMARK,
                        totalMillis = CpuBenchmarkEngine.CPU_BENCHMARK_DURATION_MS,
                    )

                    val outcome = cpuBenchmarkEngine.runCpuBenchmark(
                        mode = mode,
                        durationMillis = CpuBenchmarkEngine.CPU_BENCHMARK_DURATION_MS,
                    ) { elapsed, total, temperature, frequency ->
                        updateProgress(
                            status = BenchmarkSessionStatus.RUNNING,
                            sessionType = BenchmarkSessionType.CPU_BENCHMARK,
                            elapsedMillis = elapsed,
                            totalMillis = total,
                            temperature = temperature,
                            frequencyMhz = frequency,
                        )
                    }

                    val result = BenchmarkResult(
                        mode = mode,
                        score = BenchmarkCalculators.calculateScore(outcome.operations, outcome.durationMillis),
                        durationMillis = outcome.durationMillis,
                        peakTemperatureCelsius = outcome.peakTemperatureCelsius,
                        averageTemperatureCelsius = outcome.averageTemperatureCelsius,
                        frequencyStabilityPercent = outcome.frequencyStabilityPercent,
                        timestampMillis = System.currentTimeMillis(),
                    )

                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.COMPLETED,
                        sessionType = BenchmarkSessionType.CPU_BENCHMARK,
                        progressPercent = 100f,
                        elapsedMillis = outcome.durationMillis,
                        totalMillis = outcome.durationMillis,
                        currentTemperatureCelsius = outcome.peakTemperatureCelsius,
                        message = "Score: ${result.score}",
                    )

                    Result.Success(result)
                } catch (cancellation: CancellationException) {
                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.CANCELLED,
                        sessionType = BenchmarkSessionType.CPU_BENCHMARK,
                        message = "Benchmark cancelled",
                    )
                    throw cancellation
                } catch (exception: Exception) {
                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.FAILED,
                        sessionType = BenchmarkSessionType.CPU_BENCHMARK,
                        message = exception.message,
                    )
                    Result.Error(
                        DomainException(
                            message = exception.message ?: "CPU benchmark failed",
                            cause = exception,
                        ),
                    )
                }
            }
        }

    override suspend fun runStressTest(duration: StressTestDuration): Result<StressTestResult> =
        sessionMutex.withLock {
            if (progressState.value.status == BenchmarkSessionStatus.RUNNING) {
                return Result.Error(DomainException("Another benchmark session is already running"))
            }

            withContext(dispatcher) {
                try {
                    updateProgress(
                        status = BenchmarkSessionStatus.RUNNING,
                        sessionType = BenchmarkSessionType.STRESS_TEST,
                        totalMillis = duration.durationMillis,
                    )

                    val outcome = cpuBenchmarkEngine.runStressTest(duration) { elapsed, total, temperature, frequency ->
                        updateProgress(
                            status = BenchmarkSessionStatus.RUNNING,
                            sessionType = BenchmarkSessionType.STRESS_TEST,
                            elapsedMillis = elapsed,
                            totalMillis = total,
                            temperature = temperature,
                            frequencyMhz = frequency,
                        )
                    }

                    val result = StressTestResult(
                        duration = duration,
                        durationMillis = outcome.durationMillis,
                        peakTemperatureCelsius = outcome.peakTemperatureCelsius,
                        averageTemperatureCelsius = outcome.averageTemperatureCelsius,
                        frequencyStabilityPercent = outcome.frequencyStabilityPercent,
                        averageCpuUsagePercent = outcome.averageCpuUsagePercent,
                        timestampMillis = System.currentTimeMillis(),
                    )

                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.COMPLETED,
                        sessionType = BenchmarkSessionType.STRESS_TEST,
                        progressPercent = 100f,
                        elapsedMillis = outcome.durationMillis,
                        totalMillis = outcome.durationMillis,
                        currentTemperatureCelsius = outcome.peakTemperatureCelsius,
                        message = "Stress test completed",
                    )

                    Result.Success(result)
                } catch (cancellation: CancellationException) {
                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.CANCELLED,
                        sessionType = BenchmarkSessionType.STRESS_TEST,
                        message = "Stress test cancelled",
                    )
                    throw cancellation
                } catch (exception: Exception) {
                    progressState.value = BenchmarkProgress(
                        status = BenchmarkSessionStatus.FAILED,
                        sessionType = BenchmarkSessionType.STRESS_TEST,
                        message = exception.message,
                    )
                    Result.Error(
                        DomainException(
                            message = exception.message ?: "Stress test failed",
                            cause = exception,
                        ),
                    )
                }
            }
        }

    override suspend fun cancelSession(): Result<Unit> = safeCall {
        cpuBenchmarkEngine.requestCancel()
        if (progressState.value.status == BenchmarkSessionStatus.RUNNING) {
            progressState.value = progressState.value.copy(
                status = BenchmarkSessionStatus.CANCELLED,
                message = "Cancelling…",
            )
        }
    }

    private fun updateProgress(
        status: BenchmarkSessionStatus,
        sessionType: BenchmarkSessionType,
        elapsedMillis: Long = 0L,
        totalMillis: Long = 0L,
        temperature: Float? = null,
        frequencyMhz: Float? = null,
    ) {
        val progressPercent = if (totalMillis <= 0L) {
            0f
        } else {
            ((elapsedMillis.toFloat() / totalMillis.toFloat()) * 100f).coerceIn(0f, 100f)
        }

        progressState.value = BenchmarkProgress(
            status = status,
            sessionType = sessionType,
            progressPercent = progressPercent,
            elapsedMillis = elapsedMillis,
            totalMillis = totalMillis,
            currentTemperatureCelsius = temperature,
            currentFrequencyMhz = frequencyMhz,
        )
    }
}
