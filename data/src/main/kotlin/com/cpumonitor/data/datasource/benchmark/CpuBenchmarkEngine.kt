package com.cpumonitor.data.datasource.benchmark

import com.cpumonitor.data.datasource.proc.ProcStatDataSource
import com.cpumonitor.data.datasource.proc.ProcStatParser
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.benchmark.BenchmarkMode
import com.cpumonitor.domain.repository.ThermalRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CpuBenchmarkEngine @Inject constructor(
    private val thermalRepository: ThermalRepository,
    private val procStatDataSource: ProcStatDataSource,
) {

    private val cancelRequested = AtomicBoolean(false)

    fun requestCancel() {
        cancelRequested.set(true)
    }

    suspend fun runCpuBenchmark(
        mode: BenchmarkMode,
        durationMillis: Long,
        onProgress: suspend (elapsedMillis: Long, totalMillis: Long, temperature: Float?, frequencyMhz: Float?) -> Unit,
    ): CpuBenchmarkOutcome = coroutineScope {
        cancelRequested.set(false)
        val temperatureSamples = mutableListOf<Float>()
        val frequencySamples = mutableListOf<Float>()
        val startedAt = System.currentTimeMillis()

        val samplerJob = launch {
            collectSamples(
                totalMillis = durationMillis,
                temperatureSamples = temperatureSamples,
                frequencySamples = frequencySamples,
                onProgress = onProgress,
            )
        }

        val threadCount = when (mode) {
            BenchmarkMode.SINGLE_CORE -> 1
            BenchmarkMode.MULTI_CORE -> Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        }

        val operations = try {
            runPrimeWorkload(threadCount = threadCount, durationMillis = durationMillis)
        } finally {
            samplerJob.cancel()
        }

        if (cancelRequested.get()) {
            throw CancellationException("Benchmark cancelled")
        }

        val actualDuration = (System.currentTimeMillis() - startedAt).coerceAtLeast(1L)
        CpuBenchmarkOutcome(
            operations = operations,
            durationMillis = actualDuration,
            peakTemperatureCelsius = BenchmarkCalculators.peakTemperature(temperatureSamples),
            averageTemperatureCelsius = BenchmarkCalculators.averageTemperature(temperatureSamples),
            frequencyStabilityPercent = BenchmarkCalculators.calculateFrequencyStabilityPercent(frequencySamples),
        )
    }

    suspend fun runStressTest(
        duration: com.cpumonitor.domain.model.benchmark.StressTestDuration,
        onProgress: suspend (elapsedMillis: Long, totalMillis: Long, temperature: Float?, frequencyMhz: Float?) -> Unit,
    ): StressTestOutcome = coroutineScope {
        cancelRequested.set(false)
        val totalMillis = duration.durationMillis
        val temperatureSamples = mutableListOf<Float>()
        val frequencySamples = mutableListOf<Float>()
        val cpuUsageSamples = mutableListOf<Float>()
        val startedAt = System.currentTimeMillis()

        val samplerJob = launch {
            collectSamples(
                totalMillis = totalMillis,
                temperatureSamples = temperatureSamples,
                frequencySamples = frequencySamples,
                onProgress = onProgress,
                onCpuSample = { usage -> cpuUsageSamples += usage },
            )
        }

        val threadCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

        try {
            runPrimeWorkload(threadCount = threadCount, durationMillis = totalMillis)
        } finally {
            samplerJob.cancel()
        }

        if (cancelRequested.get()) {
            throw CancellationException("Stress test cancelled")
        }

        val actualDuration = (System.currentTimeMillis() - startedAt).coerceAtLeast(1L)
        StressTestOutcome(
            durationMillis = actualDuration,
            peakTemperatureCelsius = BenchmarkCalculators.peakTemperature(temperatureSamples),
            averageTemperatureCelsius = BenchmarkCalculators.averageTemperature(temperatureSamples),
            frequencyStabilityPercent = BenchmarkCalculators.calculateFrequencyStabilityPercent(frequencySamples),
            averageCpuUsagePercent = if (cpuUsageSamples.isEmpty()) {
                0f
            } else {
                cpuUsageSamples.average().toFloat().coerceIn(0f, 100f)
            },
        )
    }

    private suspend fun collectSamples(
        totalMillis: Long,
        temperatureSamples: MutableList<Float>,
        frequencySamples: MutableList<Float>,
        onProgress: suspend (elapsedMillis: Long, totalMillis: Long, temperature: Float?, frequencyMhz: Float?) -> Unit,
        onCpuSample: ((Float) -> Unit)? = null,
    ) {
        var previousCpuSnapshot = procStatDataSource.readSnapshot().aggregate
        val sampleStartedAt = System.currentTimeMillis()

        while (!cancelRequested.get()) {
            yield()
            val elapsed = System.currentTimeMillis() - sampleStartedAt
            if (elapsed >= totalMillis) break

            val temperature = readPeakTemperature()
            val frequency = CpuFrequencySampler.readAverageFrequencyMhz()

            temperature?.let { temperatureSamples += it }
            frequency?.let { frequencySamples += it }

            if (onCpuSample != null) {
                val currentSnapshot = procStatDataSource.readSnapshot().aggregate
                val usage = ProcStatParser.calculateUsagePercent(previousCpuSnapshot, currentSnapshot)
                onCpuSample(usage)
                previousCpuSnapshot = currentSnapshot
            }

            onProgress(elapsed, totalMillis, temperature, frequency)
            delay(SAMPLE_INTERVAL_MS)
        }
    }

    private suspend fun readPeakTemperature(): Float? {
        return when (val result = thermalRepository.getThermalZones()) {
            is Result.Success -> result.data.values.maxOrNull()
            else -> null
        }
    }

    private suspend fun runPrimeWorkload(threadCount: Int, durationMillis: Long): Long = coroutineScope {
        val deadline = System.currentTimeMillis() + durationMillis
        val totalOperations = AtomicLong(0L)

        (0 until threadCount).map { threadIndex ->
            async {
                var localOperations = 0L
                var candidate = 2L + threadIndex
                while (System.currentTimeMillis() < deadline && !cancelRequested.get()) {
                    if (isPrime(candidate)) {
                        localOperations++
                    }
                    candidate += threadCount.toLong().coerceAtLeast(1L)
                }
                totalOperations.addAndGet(localOperations)
                localOperations
            }
        }.awaitAll()

        totalOperations.get()
    }

    private fun isPrime(value: Long): Boolean {
        if (value < 2L) return false
        if (value == 2L) return true
        if (value % 2L == 0L) return false

        var divisor = 3L
        while (divisor * divisor <= value) {
            if (value % divisor == 0L) return false
            divisor += 2L
        }
        return true
    }

    data class CpuBenchmarkOutcome(
        val operations: Long,
        val durationMillis: Long,
        val peakTemperatureCelsius: Float,
        val averageTemperatureCelsius: Float,
        val frequencyStabilityPercent: Float,
    )

    data class StressTestOutcome(
        val durationMillis: Long,
        val peakTemperatureCelsius: Float,
        val averageTemperatureCelsius: Float,
        val frequencyStabilityPercent: Float,
        val averageCpuUsagePercent: Float,
    )

    companion object {
        const val CPU_BENCHMARK_DURATION_MS = 15_000L
        const val SAMPLE_INTERVAL_MS = 500L
    }
}
