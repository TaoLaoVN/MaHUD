package com.cpumonitor.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

/**
 * Low-level DataStore access for application settings and retention configuration.
 * Domain mapping is performed in the data layer.
 */
class AppPreferencesDataStore(
    private val dataStore: DataStore<Preferences>,
) {

    constructor(context: Context) : this(context.appPreferencesDataStore)

    val preferences: Flow<Preferences> = dataStore.data

    suspend fun update(transform: suspend (MutablePreferences) -> Unit) {
        dataStore.edit(transform)
    }

    fun observeString(key: Preferences.Key<String>, defaultValue: String): Flow<String> =
        preferences.map { it[key] ?: defaultValue }

    fun observeInt(key: Preferences.Key<Int>, defaultValue: Int): Flow<Int> =
        preferences.map { it[key] ?: defaultValue }
}
