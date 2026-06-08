package com.cpumonitor.domain.model.settings

/**
 * Configurable history retention windows for stored metrics.
 */
enum class RetentionPeriod {
    HOURS_24,
    DAYS_7,
    DAYS_30,
    UNLIMITED,
    ;

    /**
     * Returns the retention window in milliseconds, or null when retention is unlimited.
     */
    fun toRetentionMillis(): Long? = when (this) {
        HOURS_24 -> 24L * 60L * 60L * 1_000L
        DAYS_7 -> 7L * 24L * 60L * 60L * 1_000L
        DAYS_30 -> 30L * 24L * 60L * 60L * 1_000L
        UNLIMITED -> null
    }
}
