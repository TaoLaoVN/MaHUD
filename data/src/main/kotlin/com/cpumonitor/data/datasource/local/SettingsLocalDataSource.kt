package com.cpumonitor.data.datasource.local

import com.cpumonitor.data.datasource.LocalDataSource
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPolicy
import kotlinx.coroutines.flow.Flow

/**
 * Local persistence contract for application settings stored in DataStore.
 */
interface SettingsLocalDataSource : LocalDataSource {

    fun observeAppSettings(): Flow<AppSettings>

    fun observeRetentionPolicy(): Flow<RetentionPolicy>

    suspend fun saveAppSettings(settings: AppSettings)

    suspend fun saveRetentionPolicy(policy: RetentionPolicy)

    suspend fun saveBackgroundRefreshInterval(seconds: Int)

    suspend fun saveTheme(theme: AppTheme)
}
