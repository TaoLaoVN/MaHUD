package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.local.SettingsLocalDataSource
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPolicy
import com.cpumonitor.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    dispatchersProvider: DispatchersProvider,
    private val settingsLocalDataSource: SettingsLocalDataSource,
) : BaseRepository(dispatchersProvider.io), SettingsRepository {

    override fun observeAppSettings(): Flow<AppSettings> =
        settingsLocalDataSource.observeAppSettings()

    override fun observeRetentionPolicy(): Flow<RetentionPolicy> =
        settingsLocalDataSource.observeRetentionPolicy()

    override suspend fun updateAppSettings(settings: AppSettings): Result<Unit> =
        safeCall { settingsLocalDataSource.saveAppSettings(settings) }

    override suspend fun updateRetentionPolicy(policy: RetentionPolicy): Result<Unit> =
        safeCall { settingsLocalDataSource.saveRetentionPolicy(policy) }

    override suspend fun updateBackgroundRefreshInterval(seconds: Int): Result<Unit> =
        safeCall { settingsLocalDataSource.saveBackgroundRefreshInterval(seconds) }

    override suspend fun updateTheme(theme: AppTheme): Result<Unit> =
        safeCall { settingsLocalDataSource.saveTheme(theme) }
}
