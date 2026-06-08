package com.cpumonitor.data.datasource.local

import com.cpumonitor.domain.model.alert.AlertComparator
import com.cpumonitor.domain.model.alert.AlertMetricType
import com.cpumonitor.domain.model.alert.AlertRule

internal object AlertRulesCodec {

    private const val FIELD_SEPARATOR = "|"
    private const val RULE_SEPARATOR = ";;"

    fun encode(rules: List<AlertRule>): String =
        rules.joinToString(RULE_SEPARATOR) { rule ->
            listOf(
                rule.id,
                rule.metricType.name,
                rule.threshold.toString(),
                rule.comparator.name,
                rule.enabled.toString(),
                rule.label.replace(FIELD_SEPARATOR, "/"),
            ).joinToString(FIELD_SEPARATOR)
        }

    fun decode(raw: String): List<AlertRule> {
        if (raw.isBlank()) return emptyList()
        return raw.split(RULE_SEPARATOR)
            .mapNotNull { entry -> decodeRule(entry) }
    }

    private fun decodeRule(entry: String): AlertRule? {
        val parts = entry.split(FIELD_SEPARATOR)
        if (parts.size < 6) return null

        val metricType = runCatching { AlertMetricType.valueOf(parts[1]) }.getOrNull() ?: return null
        val comparator = runCatching { AlertComparator.valueOf(parts[3]) }.getOrNull() ?: return null
        val threshold = parts[2].toFloatOrNull() ?: return null
        val enabled = parts[4].toBooleanStrictOrNull() ?: true

        return AlertRule(
            id = parts[0],
            metricType = metricType,
            threshold = threshold,
            comparator = comparator,
            enabled = enabled,
            label = parts[5],
        )
    }
}
