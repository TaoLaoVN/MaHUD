package com.cpumonitor.core.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preference keys for application settings and retention policies.
 */
object AppPreferencesKeys {
    val THEME = stringPreferencesKey("theme")
    val BACKGROUND_REFRESH_INTERVAL_SECONDS = intPreferencesKey("background_refresh_interval_seconds")
    val CPU_RETENTION = stringPreferencesKey("cpu_retention")
    val MEMORY_RETENTION = stringPreferencesKey("memory_retention")
    val THERMAL_RETENTION = stringPreferencesKey("thermal_retention")
    val BATTERY_RETENTION = stringPreferencesKey("battery_retention")
    val ALERT_RULES = stringPreferencesKey("alert_rules")
}
