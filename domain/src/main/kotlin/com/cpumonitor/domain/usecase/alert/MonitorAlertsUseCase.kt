package com.cpumonitor.domain.usecase.alert

import com.cpumonitor.domain.alert.AlertEvaluator
import com.cpumonitor.domain.gateway.AlertNotificationController
import com.cpumonitor.domain.model.BatteryMetrics
import com.cpumonitor.domain.model.CpuUsageMetrics
import com.cpumonitor.domain.model.MemoryMetrics
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.ThermalMetrics
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.repository.AlertRepository
import com.cpumonitor.domain.repository.BatteryRepository
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.repository.MemoryRepository
import com.cpumonitor.domain.repository.MonitoringConstants
import com.cpumonitor.domain.repository.ThermalRepository
import com.cpumonitor.domain.usecase.FlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class MonitorAlertsParams(
    val intervalMs: Long = MonitoringConstants.DEFAULT_REFRESH_INTERVAL_MS,
)

/**
 * Evaluates enabled alert rules against realtime metrics and posts notifications.
 */
class MonitorAlertsUseCase @Inject constructor(
    private val cpuRepository: CpuRepository,
    private val memoryRepository: MemoryRepository,
    private val thermalRepository: ThermalRepository,
    private val batteryRepository: BatteryRepository,
    private val alertRepository: AlertRepository,
    private val alertNotificationController: AlertNotificationController,
) : FlowUseCase<MonitorAlertsParams, Unit>(Dispatchers.Default) {

    private val lastTriggeredAt = mutableMapOf<String, Long>()

    override fun execute(params: MonitorAlertsParams): Flow<Unit> = channelFlow {
        val intervalMs = params.intervalMs.coerceAtLeast(MonitoringConstants.MIN_REFRESH_INTERVAL_MS)

        val metricsJob = launch {
            combine(
                cpuRepository.observeCpuUsage(intervalMs),
                memoryRepository.observeMemoryUsage(intervalMs),
                thermalRepository.observeThermal(intervalMs),
                batteryRepository.observeBatteryStatus(intervalMs),
                alertRepository.observeAlertRules(),
            ) { cpuResult, memoryResult, thermalResult, batteryResult, rules ->
                AlertSnapshot(cpuResult, memoryResult, thermalResult, batteryResult, rules)
            }.collect { snapshot ->
                evaluateSnapshot(snapshot)
                send(Unit)
            }
        }

        awaitClose { metricsJob.cancel() }
    }

    private suspend fun evaluateSnapshot(snapshot: AlertSnapshot) {
        val cpu = (snapshot.cpu as? Result.Success)?.data ?: return
        val memory = (snapshot.memory as? Result.Success)?.data ?: return
        val thermal = (snapshot.thermal as? Result.Success)?.data ?: return
        val battery = (snapshot.battery as? Result.Success)?.data ?: return

        val memoryPercent = toUsedPercent(memory)
        val thermalValue = thermal.cpuTemperatureCelsius.takeIf { it > 0f } ?: thermal.batteryTemperatureCelsius

        snapshot.rules.forEach { rule ->
            if (!AlertEvaluator.isTriggered(rule, cpu.totalUsagePercent, memoryPercent, thermalValue, battery.percentage.toFloat())) {
                return@forEach
            }

            val now = System.currentTimeMillis()
            val lastTriggered = lastTriggeredAt[rule.id] ?: 0L
            if (now - lastTriggered < ALERT_COOLDOWN_MS) return@forEach

            val metricValue = AlertEvaluator.metricValue(
                rule,
                cpu.totalUsagePercent,
                memoryPercent,
                thermalValue,
                battery.percentage.toFloat(),
            )
            val message = "${rule.label} (current=${formatMetric(rule, metricValue)})"

            alertNotificationController.showAlert(
                title = "CPU Monitor Alert",
                message = message,
                notificationId = rule.id.hashCode(),
            )

            alertRepository.insertAlertHistory(
                AlertHistoryEntry(
                    ruleId = rule.id,
                    message = message,
                    metricValue = metricValue,
                    timestampMillis = now,
                ),
            )

            lastTriggeredAt[rule.id] = now
        }
    }

    private fun toUsedPercent(memory: MemoryMetrics): Float {
        val denominator = memory.usedBytes + memory.availableBytes
        if (denominator <= 0L) return 0f
        return ((memory.usedBytes.toDouble() / denominator.toDouble()) * 100.0).roundToInt().toFloat()
    }

    private fun formatMetric(rule: AlertRule, value: Float): String =
        if (rule.metricType.name == "THERMAL") {
            "${value.toInt()}°C"
        } else {
            "${value.toInt()}%"
        }

    private data class AlertSnapshot(
        val cpu: Result<CpuUsageMetrics>,
        val memory: Result<MemoryMetrics>,
        val thermal: Result<ThermalMetrics>,
        val battery: Result<BatteryMetrics>,
        val rules: List<AlertRule>,
    )

    private companion object {
        const val ALERT_COOLDOWN_MS = 5 * 60_000L
    }
}
