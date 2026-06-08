package com.cpumonitor.domain.model.history

/**
 * Selectable history windows aligned with the CPU module roadmap.
 */
enum class HistoryTimeRange(
    val displayName: String,
    val durationMillis: Long,
) {
    ONE_MINUTE("1 min", 60_000L),
    FIVE_MINUTES("5 min", 5 * 60_000L),
    FIFTEEN_MINUTES("15 min", 15 * 60_000L),
    ONE_HOUR("1 hour", 60 * 60_000L),
    TWENTY_FOUR_HOURS("24 hours", 24 * 60 * 60_000L),
}
