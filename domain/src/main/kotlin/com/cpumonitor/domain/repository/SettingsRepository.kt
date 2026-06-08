package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPolicy
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for application settings and retention policy configuration.
 */
interface SettingsRepository : Repository {

    fun observeAppSettings(): Flow<AppSettings>

    fun observeRetentionPolicy(): Flow<RetentionPolicy>

    suspend fun updateAppSettings(settings: AppSettings): Result<Unit>

    suspend fun updateRetentionPolicy(policy: RetentionPolicy): Result<Unit>

    suspend fun updateBackgroundRefreshInterval(seconds: Int): Result<Unit>

    suspend fun updateTheme(theme: AppTheme): Result<Unit>
}
