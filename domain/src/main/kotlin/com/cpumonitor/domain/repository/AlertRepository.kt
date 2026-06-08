package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.alert.AlertHistoryEntry
import com.cpumonitor.domain.model.alert.AlertRule
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for alert rules and alert history.
 */
interface AlertRepository : Repository {

    fun observeAlertRules(): Flow<List<AlertRule>>

    suspend fun saveAlertRule(rule: AlertRule): Result<Unit>

    suspend fun deleteAlertRule(ruleId: String): Result<Unit>

    suspend fun ensureDefaultRules(): Result<Unit>

    fun observeAlertHistory(limit: Int = 100): Flow<List<AlertHistoryEntry>>

    suspend fun insertAlertHistory(entry: AlertHistoryEntry): Result<Long>
}
