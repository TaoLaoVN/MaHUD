package com.cpumonitor.data.mapper

import androidx.datastore.preferences.core.Preferences
import com.cpumonitor.core.datastore.AppPreferencesKeys
import com.cpumonitor.core.datastore.AppPreferencesDataStore
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPeriod
import com.cpumonitor.domain.model.settings.RetentionPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun Preferences.toAppSettings(): AppSettings =
    AppSettings(
        theme = this[AppPreferencesKeys.THEME].toAppTheme(),
        backgroundRefreshIntervalSeconds =
            this[AppPreferencesKeys.BACKGROUND_REFRESH_INTERVAL_SECONDS]
                ?: AppSettings.DEFAULT_BACKGROUND_REFRESH_SECONDS,
        retentionPolicy = toRetentionPolicy(),
    )

internal fun Preferences.toRetentionPolicy(): RetentionPolicy =
    RetentionPolicy(
        cpuRetention = this[AppPreferencesKeys.CPU_RETENTION].toRetentionPeriod(),
        memoryRetention = this[AppPreferencesKeys.MEMORY_RETENTION].toRetentionPeriod(),
        thermalRetention = this[AppPreferencesKeys.THERMAL_RETENTION].toRetentionPeriod(),
        batteryRetention = this[AppPreferencesKeys.BATTERY_RETENTION].toRetentionPeriod(),
    )

internal fun AppPreferencesDataStore.observeAppSettings(): Flow<AppSettings> =
    preferences.map { it.toAppSettings() }

internal fun AppPreferencesDataStore.observeRetentionPolicy(): Flow<RetentionPolicy> =
    preferences.map { it.toRetentionPolicy() }

internal suspend fun AppPreferencesDataStore.saveAppSettings(settings: AppSettings) {
    update { preferences ->
        preferences[AppPreferencesKeys.THEME] = settings.theme.name
        preferences[AppPreferencesKeys.BACKGROUND_REFRESH_INTERVAL_SECONDS] =
            settings.backgroundRefreshIntervalSeconds.coerceIn(
                AppSettings.MIN_BACKGROUND_REFRESH_SECONDS,
                AppSettings.MAX_BACKGROUND_REFRESH_SECONDS,
            )
        preferences[AppPreferencesKeys.CPU_RETENTION] = settings.retentionPolicy.cpuRetention.name
        preferences[AppPreferencesKeys.MEMORY_RETENTION] = settings.retentionPolicy.memoryRetention.name
        preferences[AppPreferencesKeys.THERMAL_RETENTION] = settings.retentionPolicy.thermalRetention.name
        preferences[AppPreferencesKeys.BATTERY_RETENTION] = settings.retentionPolicy.batteryRetention.name
    }
}

internal suspend fun AppPreferencesDataStore.saveRetentionPolicy(policy: RetentionPolicy) {
    update { preferences ->
        preferences[AppPreferencesKeys.CPU_RETENTION] = policy.cpuRetention.name
        preferences[AppPreferencesKeys.MEMORY_RETENTION] = policy.memoryRetention.name
        preferences[AppPreferencesKeys.THERMAL_RETENTION] = policy.thermalRetention.name
        preferences[AppPreferencesKeys.BATTERY_RETENTION] = policy.batteryRetention.name
    }
}

internal suspend fun AppPreferencesDataStore.saveBackgroundRefreshInterval(seconds: Int) {
    update { preferences ->
        preferences[AppPreferencesKeys.BACKGROUND_REFRESH_INTERVAL_SECONDS] =
            seconds.coerceIn(
                AppSettings.MIN_BACKGROUND_REFRESH_SECONDS,
                AppSettings.MAX_BACKGROUND_REFRESH_SECONDS,
            )
    }
}

internal suspend fun AppPreferencesDataStore.saveTheme(theme: AppTheme) {
    update { preferences ->
        preferences[AppPreferencesKeys.THEME] = theme.name
    }
}

private fun String?.toAppTheme(): AppTheme =
    runCatching { AppTheme.valueOf(this ?: AppTheme.DARK.name) }
        .getOrDefault(AppTheme.DARK)

private fun String?.toRetentionPeriod(): RetentionPeriod =
    runCatching { RetentionPeriod.valueOf(this ?: RetentionPeriod.DAYS_7.name) }
        .getOrDefault(RetentionPeriod.DAYS_7)
