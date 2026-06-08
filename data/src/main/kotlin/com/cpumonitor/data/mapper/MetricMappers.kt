package com.cpumonitor.data.mapper

import com.cpumonitor.core.database.entity.BatteryMetricEntity
import com.cpumonitor.core.database.entity.CpuMetricEntity
import com.cpumonitor.core.database.entity.MemoryMetricEntity
import com.cpumonitor.core.database.entity.ThermalMetricEntity
import com.cpumonitor.domain.model.metric.BatteryMetric
import com.cpumonitor.domain.model.metric.CpuMetric
import com.cpumonitor.domain.model.metric.MemoryMetric
import com.cpumonitor.domain.model.metric.ThermalMetric

internal fun CpuMetricEntity.toDomain(): CpuMetric =
    CpuMetric(
        id = id,
        timestampMillis = timestampMillis,
        totalUsagePercent = totalUsagePercent,
        perCoreUsagePercent = perCoreUsagePercent,
        bigCoreUsagePercent = bigCoreUsagePercent,
        littleCoreUsagePercent = littleCoreUsagePercent,
        currentFrequencyMhz = currentFrequencyMhz,
        minFrequencyMhz = minFrequencyMhz,
        maxFrequencyMhz = maxFrequencyMhz,
    )

internal fun CpuMetric.toEntity(): CpuMetricEntity =
    CpuMetricEntity(
        id = id,
        timestampMillis = timestampMillis,
        totalUsagePercent = totalUsagePercent,
        perCoreUsagePercent = perCoreUsagePercent,
        bigCoreUsagePercent = bigCoreUsagePercent,
        littleCoreUsagePercent = littleCoreUsagePercent,
        currentFrequencyMhz = currentFrequencyMhz,
        minFrequencyMhz = minFrequencyMhz,
        maxFrequencyMhz = maxFrequencyMhz,
    )

internal fun MemoryMetricEntity.toDomain(): MemoryMetric =
    MemoryMetric(
        id = id,
        timestampMillis = timestampMillis,
        usedBytes = usedBytes,
        availableBytes = availableBytes,
        freeBytes = freeBytes,
        cachedBytes = cachedBytes,
    )

internal fun MemoryMetric.toEntity(): MemoryMetricEntity =
    MemoryMetricEntity(
        id = id,
        timestampMillis = timestampMillis,
        usedBytes = usedBytes,
        availableBytes = availableBytes,
        freeBytes = freeBytes,
        cachedBytes = cachedBytes,
    )

internal fun ThermalMetricEntity.toDomain(): ThermalMetric =
    ThermalMetric(
        id = id,
        timestampMillis = timestampMillis,
        cpuTemperatureCelsius = cpuTemperatureCelsius,
        batteryTemperatureCelsius = batteryTemperatureCelsius,
        thermalZoneTemperatures = thermalZoneTemperatures,
        isThrottling = isThrottling,
        isOverheating = isOverheating,
    )

internal fun ThermalMetric.toEntity(): ThermalMetricEntity =
    ThermalMetricEntity(
        id = id,
        timestampMillis = timestampMillis,
        cpuTemperatureCelsius = cpuTemperatureCelsius,
        batteryTemperatureCelsius = batteryTemperatureCelsius,
        thermalZoneTemperatures = thermalZoneTemperatures,
        isThrottling = isThrottling,
        isOverheating = isOverheating,
    )

internal fun BatteryMetricEntity.toDomain(): BatteryMetric =
    BatteryMetric(
        id = id,
        timestampMillis = timestampMillis,
        percentage = percentage,
        voltageMv = voltageMv,
        currentMa = currentMa,
        capacityMah = capacityMah,
        temperatureCelsius = temperatureCelsius,
        health = health,
        isCharging = isCharging,
        chargeSpeedMw = chargeSpeedMw,
    )

internal fun BatteryMetric.toEntity(): BatteryMetricEntity =
    BatteryMetricEntity(
        id = id,
        timestampMillis = timestampMillis,
        percentage = percentage,
        voltageMv = voltageMv,
        currentMa = currentMa,
        capacityMah = capacityMah,
        temperatureCelsius = temperatureCelsius,
        health = health,
        isCharging = isCharging,
        chargeSpeedMw = chargeSpeedMw,
    )
