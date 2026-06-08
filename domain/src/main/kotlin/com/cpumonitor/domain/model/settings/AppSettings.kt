package com.cpumonitor.domain.model.settings

/**
 * User-configurable application settings persisted via DataStore.
 */
data class AppSettings(
    val theme: AppTheme = AppTheme.DARK,
    val backgroundRefreshIntervalSeconds: Int = DEFAULT_BACKGROUND_REFRESH_SECONDS,
    val retentionPolicy: RetentionPolicy = RetentionPolicy(),
) {
    companion object {
        const val MIN_BACKGROUND_REFRESH_SECONDS = 5
        const val MAX_BACKGROUND_REFRESH_SECONDS = 60
        const val DEFAULT_BACKGROUND_REFRESH_SECONDS = 15
    }
}
