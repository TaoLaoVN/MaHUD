package com.cpumonitor.data.repository

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.local.AlertLocalDataSource
import com.cpumonitor.data.datasource.local.AlertRulesCodec
import com.cpumonitor.core.datastore.AppPreferencesDataStore
import com.cpumonitor.core.datastore.AppPreferencesKeys
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.model.alert.DefaultAlertRules
import com.cpumonitor.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertLocalDataSource: AlertLocalDataSource,
    private val appPreferencesDataStore: AppPreferencesDataStore,
    dispatchersProvider: DispatchersProvider,
) : BaseRepository(dispatchersProvider.io), AlertRepository {

    override fun observeAlertRules(): Flow<List<AlertRule>> =
        alertLocalDataSource.observeAlertRules()

    override suspend fun saveAlertRule(rule: AlertRule): Result<Unit> =
        safeCall {
            val current = readStoredRules().toMutableList()
            val index = current.indexOfFirst { it.id == rule.id }
            if (index >= 0) {
                current[index] = rule
            } else {
                current += rule
            }
            alertLocalDataSource.saveAlertRules(current)
        }

    override suspend fun deleteAlertRule(ruleId: String): Result<Unit> =
        safeCall {
            val updated = readStoredRules().filterNot { it.id == ruleId }
            alertLocalDataSource.saveAlertRules(updated)
        }

    override suspend fun ensureDefaultRules(): Result<Unit> =
        safeCall {
            val raw = appPreferencesDataStore.preferences.first()[AppPreferencesKeys.ALERT_RULES]
            if (raw.isNullOrBlank()) {
                alertLocalDataSource.saveAlertRules(DefaultAlertRules.presets)
            }
        }

    override fun observeAlertHistory(limit: Int): Flow<List<AlertHistoryEntry>> =
        alertLocalDataSource.observeAlertHistory(limit)

    override suspend fun insertAlertHistory(entry: AlertHistoryEntry): Result<Long> =
        safeCall { alertLocalDataSource.insertAlertHistory(entry) }

    private suspend fun readStoredRules(): List<AlertRule> {
        val raw = appPreferencesDataStore.preferences.first()[AppPreferencesKeys.ALERT_RULES].orEmpty()
        val decoded = AlertRulesCodec.decode(raw)
        return decoded.ifEmpty { DefaultAlertRules.presets }
    }
}
