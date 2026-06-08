package com.cpumonitor.data.datasource.local

import com.cpumonitor.core.datastore.AppPreferencesDataStore
import com.cpumonitor.data.mapper.observeAppSettings
import com.cpumonitor.data.mapper.observeRetentionPolicy
import com.cpumonitor.data.mapper.saveAppSettings
import com.cpumonitor.data.mapper.saveBackgroundRefreshInterval
import com.cpumonitor.data.mapper.saveRetentionPolicy
import com.cpumonitor.data.mapper.saveTheme
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPolicy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsLocalDataSourceImpl @Inject constructor(
    private val appPreferencesDataStore: AppPreferencesDataStore,
) : SettingsLocalDataSource {

    override fun observeAppSettings(): Flow<AppSettings> =
        appPreferencesDataStore.observeAppSettings()

    override fun observeRetentionPolicy(): Flow<RetentionPolicy> =
        appPreferencesDataStore.observeRetentionPolicy()

    override suspend fun saveAppSettings(settings: AppSettings) {
        appPreferencesDataStore.saveAppSettings(settings)
    }

    override suspend fun saveRetentionPolicy(policy: RetentionPolicy) {
        appPreferencesDataStore.saveRetentionPolicy(policy)
    }

    override suspend fun saveBackgroundRefreshInterval(seconds: Int) {
        appPreferencesDataStore.saveBackgroundRefreshInterval(seconds)
    }

    override suspend fun saveTheme(theme: AppTheme) {
        appPreferencesDataStore.saveTheme(theme)
    }
}
