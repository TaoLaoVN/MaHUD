package com.cpumonitor.data.datasource.local

import com.cpumonitor.core.datastore.AppPreferencesDataStore
import com.cpumonitor.core.datastore.AppPreferencesKeys
import com.cpumonitor.core.database.dao.AlertHistoryDao
import com.cpumonitor.core.database.entity.AlertHistoryEntity
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.alert.AlertRule
import com.cpumonitor.domain.model.alert.DefaultAlertRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlertLocalDataSource @Inject constructor(
    private val appPreferencesDataStore: AppPreferencesDataStore,
    private val alertHistoryDao: AlertHistoryDao,
) {

    fun observeAlertRules(): Flow<List<AlertRule>> =
        appPreferencesDataStore.observeString(AppPreferencesKeys.ALERT_RULES, "")
            .map { raw ->
                val decoded = AlertRulesCodec.decode(raw)
                if (decoded.isEmpty()) DefaultAlertRules.presets else decoded
            }

    suspend fun saveAlertRules(rules: List<AlertRule>) {
        appPreferencesDataStore.update { prefs ->
            prefs[AppPreferencesKeys.ALERT_RULES] = AlertRulesCodec.encode(rules)
        }
    }

    fun observeAlertHistory(limit: Int): Flow<List<AlertHistoryEntry>> =
        alertHistoryDao.observeRecent(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun insertAlertHistory(entry: AlertHistoryEntry): Long =
        alertHistoryDao.insert(entry.toEntity())
}

private fun AlertHistoryEntity.toDomain(): AlertHistoryEntry =
    AlertHistoryEntry(
        id = id,
        ruleId = ruleId,
        message = message,
        metricValue = metricValue,
        timestampMillis = timestampMillis,
    )

private fun AlertHistoryEntry.toEntity(): AlertHistoryEntity =
    AlertHistoryEntity(
        id = id,
        ruleId = ruleId,
        message = message,
        metricValue = metricValue,
        timestampMillis = timestampMillis,
    )
