package com.cpumonitor.data.repository

import com.cpumonitor.core.testing.TestDispatchersProvider
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.settings.AppSettings
import com.cpumonitor.domain.model.settings.AppTheme
import com.cpumonitor.domain.model.settings.RetentionPeriod
import com.cpumonitor.domain.model.settings.RetentionPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val dataSource = FakeSettingsLocalDataSource()
    private val repository = SettingsRepositoryImpl(
        dispatchersProvider = TestDispatchersProvider(io = dispatcher),
        settingsLocalDataSource = dataSource,
    )

    @Test
    fun observeAppSettings_emitsPersistedSettings() = runTest(dispatcher) {
        val settings = AppSettings(
            theme = AppTheme.AMOLED,
            backgroundRefreshIntervalSeconds = 30,
        )
        dataSource.emit(settings)

        val observed = repository.observeAppSettings().first()

        assertEquals(settings, observed)
    }

    @Test
    fun updateTheme_persistsThemeChange() = runTest(dispatcher) {
        val result = repository.updateTheme(AppTheme.LIGHT)

        assertTrue(result is Result.Success)
        assertEquals(AppTheme.LIGHT, dataSource.currentSettings.theme)
    }

    @Test
    fun updateRetentionPolicy_persistsAllMetricPolicies() = runTest(dispatcher) {
        val policy = RetentionPolicy(
            cpuRetention = RetentionPeriod.HOURS_24,
            memoryRetention = RetentionPeriod.DAYS_30,
            thermalRetention = RetentionPeriod.UNLIMITED,
            batteryRetention = RetentionPeriod.DAYS_7,
        )

        val result = repository.updateRetentionPolicy(policy)

        assertTrue(result is Result.Success)
        assertEquals(policy, dataSource.currentSettings.retentionPolicy)
    }
}

private class FakeSettingsLocalDataSource : com.cpumonitor.data.datasource.local.SettingsLocalDataSource {
    private val settingsFlow = MutableStateFlow(AppSettings())

    val currentSettings: AppSettings
        get() = settingsFlow.value

    fun emit(settings: AppSettings) {
        settingsFlow.value = settings
    }

    override fun observeAppSettings(): Flow<AppSettings> = settingsFlow

    override fun observeRetentionPolicy(): Flow<RetentionPolicy> =
        settingsFlow.map { it.retentionPolicy }

    override suspend fun saveAppSettings(settings: AppSettings) {
        settingsFlow.value = settings
    }

    override suspend fun saveRetentionPolicy(policy: RetentionPolicy) {
        settingsFlow.value = settingsFlow.value.copy(retentionPolicy = policy)
    }

    override suspend fun saveBackgroundRefreshInterval(seconds: Int) {
        settingsFlow.value = settingsFlow.value.copy(
            backgroundRefreshIntervalSeconds = seconds,
        )
    }

    override suspend fun saveTheme(theme: AppTheme) {
        settingsFlow.value = settingsFlow.value.copy(theme = theme)
    }
}
