package com.cpumonitor.core.database.di

import android.content.Context
import androidx.room.Room
import com.cpumonitor.core.database.CPUMonitorDatabase
import com.cpumonitor.core.database.dao.AlertHistoryDao
import com.cpumonitor.core.database.dao.BatteryMetricsDao
import com.cpumonitor.core.database.dao.CpuMetricsDao
import com.cpumonitor.core.database.dao.MemoryMetricsDao
import com.cpumonitor.core.database.dao.ThermalMetricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "cpu_monitor.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CPUMonitorDatabase =
        Room.databaseBuilder(
            context,
            CPUMonitorDatabase::class.java,
            DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCpuMetricsDao(database: CPUMonitorDatabase): CpuMetricsDao =
        database.cpuMetricsDao()

    @Provides
    fun provideMemoryMetricsDao(database: CPUMonitorDatabase): MemoryMetricsDao =
        database.memoryMetricsDao()

    @Provides
    fun provideThermalMetricsDao(database: CPUMonitorDatabase): ThermalMetricsDao =
        database.thermalMetricsDao()

    @Provides
    fun provideBatteryMetricsDao(database: CPUMonitorDatabase): BatteryMetricsDao =
        database.batteryMetricsDao()

    @Provides
    fun provideAlertHistoryDao(database: CPUMonitorDatabase): AlertHistoryDao =
        database.alertHistoryDao()
}
