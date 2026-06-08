package com.cpumonitor.domain.usecase.monitoring

import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.CpuArchitectureInfo
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric
import com.cpumonitor.domain.repository.BatteryRepository
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.repository.MemoryRepository
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ThermalRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersistPolledMetricsParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

private const val OVERHEATING_THRESHOLD_CELSIUS = 45f

/**
 * Polls realtime CPU, memory, thermal, and battery metrics and persists each sample.
 */
class PersistPolledMetricsUseCase @Inject constructor(
    private val cpuRepository: CpuRepository,
    private val memoryRepository: MemoryRepository,
    private val thermalRepository: ThermalRepository,
    private val batteryRepository: BatteryRepository,
    private val metricsRepository: MetricsRepository,
) : FlowUseCase<PersistPolledMetricsParams, Unit>(Dispatchers.IO) {

    override fun execute(params: PersistPolledMetricsParams): Flow<Unit> = channelFlow {
        val intervalMs = params.intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)
        var cpuArchitecture: CpuArchitectureInfo? = null

        val cpuJob = launch {
            cpuRepository.observeCpuUsage(intervalMs).collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (cpuArchitecture == null) {
                            cpuArchitecture = resolveCpuArchitecture()
                        }
                        persistCpuSample(result.data, cpuArchitecture)
                        send(Unit)
                    }
                    is Result.Error -> close(result.exception)
                    Result.Loading -> Unit
                }
            }
        }

        val memoryJob = launch {
            memoryRepository.observeMemoryUsage(intervalMs).collect { result ->
                when (result) {
                    is Result.Success -> {
                        persistMemorySample(result.data)
                        send(Unit)
                    }
                    is Result.Error -> close(result.exception)
                    Result.Loading -> Unit
                }
            }
        }

        val thermalJob = launch {
            thermalRepository.observeThermal(intervalMs).collect { result ->
                when (result) {
                    is Result.Success -> {
                        persistThermalSample(result.data)
                        send(Unit)
                    }
                    is Result.Error -> close(result.exception)
                    Result.Loading -> Unit
                }
            }
        }

        val batteryJob = launch {
            batteryRepository.observeBatteryStatus(intervalMs).collect { result ->
                when (result) {
                    is Result.Success -> {
                        persistBatterySample(result.data)
                        send(Unit)
                    }
                    is Result.Error -> close(result.exception)
                    Result.Loading -> Unit
                }
            }
        }

        awaitClose {
            cpuJob.cancel()
            memoryJob.cancel()
            thermalJob.cancel()
            batteryJob.cancel()
        }
    }

    private suspend fun resolveCpuArchitecture(): CpuArchitectureInfo? =
        when (val result = cpuRepository.getCpuArchitecture()) {
            is Result.Success -> result.data
            else -> null
        }

    private suspend fun persistCpuSample(
        usage: CpuUsageMetrics,
        architecture: CpuArchitectureInfo?,
    ) {
        val insertResult = metricsRepository.insertCpuMetric(
            usage.toPersistedCpuMetric(architecture),
        )
        if (insertResult is Result.Error) {
            throw insertResult.exception
        }
    }

    private suspend fun persistMemorySample(metrics: MemoryMetrics) {
        val insertResult = metricsRepository.insertMemoryMetric(metrics.toPersistedMemoryMetric())
        if (insertResult is Result.Error) {
            throw insertResult.exception
        }
    }

    private suspend fun persistThermalSample(metrics: ThermalMetrics) {
        val zones = when (val result = thermalRepository.getThermalZones()) {
            is Result.Success -> result.data
            else -> emptyMap()
        }
        val insertResult = metricsRepository.insertThermalMetric(
            metrics.toPersistedThermalMetric(zones),
        )
        if (insertResult is Result.Error) {
            throw insertResult.exception
        }
    }

    private suspend fun persistBatterySample(metrics: BatteryMetrics) {
        val insertResult = metricsRepository.insertBatteryMetric(
            metrics.toPersistedBatteryMetric(),
        )
        if (insertResult is Result.Error) {
            throw insertResult.exception
        }
    }
}

private fun CpuUsageMetrics.toPersistedCpuMetric(
    architecture: CpuArchitectureInfo?,
): CpuMetric {
    val coreCount = perCoreUsagePercent.size.coerceAtLeast(1)
    val littleCoreCount = coreCount / 2
    val littleCores = perCoreUsagePercent.take(littleCoreCount)
    val bigCores = perCoreUsagePercent.drop(littleCoreCount)

    val frequencies = architecture?.processors
        ?.mapNotNull { it.currentFrequencyMhz }
        .orEmpty()
    val maxFrequencies = architecture?.processors
        ?.mapNotNull { it.maxFrequencyMhz }
        .orEmpty()

    return CpuMetric(
        timestampMillis = timestampMillis,
        totalUsagePercent = totalUsagePercent.sanitizeMetric(),
        perCoreUsagePercent = perCoreUsagePercent.map { it.sanitizeMetric() },
        bigCoreUsagePercent = if (bigCores.isNotEmpty()) {
            bigCores.average().toFloat().sanitizeMetric()
        } else {
            totalUsagePercent.sanitizeMetric()
        },
        littleCoreUsagePercent = if (littleCores.isNotEmpty()) {
            littleCores.average().toFloat().sanitizeMetric()
        } else {
            totalUsagePercent.sanitizeMetric()
        },
        currentFrequencyMhz = frequencies.averageOrZero(),
        minFrequencyMhz = maxFrequencies.minOrNull()?.sanitizeMetric() ?: 0f,
        maxFrequencyMhz = maxFrequencies.maxOrNull()?.sanitizeMetric() ?: 0f,
    )
}

private fun List<Float>.averageOrZero(): Float =
    if (isEmpty()) 0f else average().toFloat().sanitizeMetric()

private fun Float.sanitizeMetric(): Float = when {
    isNaN() || isInfinite() -> 0f
    else -> this
}

private fun MemoryMetrics.toPersistedMemoryMetric(): MemoryMetric =
    MemoryMetric(
        timestampMillis = timestampMillis,
        usedBytes = usedBytes,
        availableBytes = availableBytes,
        freeBytes = freeBytes,
        cachedBytes = cachedBytes,
    )

private fun ThermalMetrics.toPersistedThermalMetric(
    zones: Map<String, Float>,
): ThermalMetric =
    ThermalMetric(
        timestampMillis = timestampMillis,
        cpuTemperatureCelsius = cpuTemperatureCelsius,
        batteryTemperatureCelsius = batteryTemperatureCelsius,
        thermalZoneTemperatures = zones,
        isThrottling = false,
        isOverheating = cpuTemperatureCelsius > OVERHEATING_THRESHOLD_CELSIUS,
    )

private fun BatteryMetrics.toPersistedBatteryMetric(): BatteryMetric =
    BatteryMetric(
        timestampMillis = timestampMillis,
        percentage = percentage,
        voltageMv = voltageMv,
        currentMa = currentMa,
        capacityMah = 0,
        temperatureCelsius = temperatureCelsius,
        health = health,
        isCharging = isCharging,
        chargeSpeedMw = chargeSpeedMw,
    )
