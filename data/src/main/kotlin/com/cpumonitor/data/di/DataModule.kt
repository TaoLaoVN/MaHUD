package com.cpumonitor.data.di

import com.cpumonitor.data.datasource.local.MetricLocalDataSource
import com.cpumonitor.data.datasource.local.MetricLocalDataSourceImpl
import com.cpumonitor.data.datasource.local.SettingsLocalDataSource
import com.cpumonitor.data.datasource.local.SettingsLocalDataSourceImpl
import com.cpumonitor.data.repository.MetricsRepositoryImpl
import com.cpumonitor.data.repository.SettingsRepositoryImpl
import com.cpumonitor.data.repository.AlertRepositoryImpl
import com.cpumonitor.data.repository.AppUpdateRepositoryImpl
import com.cpumonitor.data.repository.AnalyticsRepositoryImpl
import com.cpumonitor.data.repository.BenchmarkRepositoryImpl
import com.cpumonitor.data.repository.BatteryRepositoryImpl
import com.cpumonitor.data.repository.CpuRepositoryImpl
import com.cpumonitor.data.repository.DeviceRepositoryImpl
import com.cpumonitor.data.repository.ExportRepositoryImpl
import com.cpumonitor.data.repository.MemoryRepositoryImpl
import com.cpumonitor.data.repository.OverlayRepositoryImpl
import com.cpumonitor.data.repository.ProcessRepositoryImpl
import com.cpumonitor.data.repository.StorageRepositoryImpl
import com.cpumonitor.data.repository.ThermalRepositoryImpl
import com.cpumonitor.domain.repository.AlertRepository
import com.cpumonitor.domain.repository.AppUpdateRepository
import com.cpumonitor.domain.repository.AnalyticsRepository
import com.cpumonitor.domain.repository.BenchmarkRepository
import com.cpumonitor.domain.repository.BatteryRepository
import com.cpumonitor.domain.repository.CpuRepository
import com.cpumonitor.domain.repository.DeviceRepository
import com.cpumonitor.domain.repository.ExportRepository
import com.cpumonitor.domain.repository.MemoryRepository
import com.cpumonitor.domain.repository.MetricsRepository
import com.cpumonitor.domain.repository.OverlayRepository
import com.cpumonitor.domain.repository.ProcessRepository
import com.cpumonitor.domain.repository.SettingsRepository
import com.cpumonitor.domain.repository.StorageRepository
import com.cpumonitor.domain.repository.ThermalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMetricLocalDataSource(
        impl: MetricLocalDataSourceImpl,
    ): MetricLocalDataSource

    @Binds
    @Singleton
    abstract fun bindSettingsLocalDataSource(
        impl: SettingsLocalDataSourceImpl,
    ): SettingsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindMetricsRepository(
        impl: MetricsRepositoryImpl,
    ): MetricsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCpuRepository(
        impl: CpuRepositoryImpl,
    ): CpuRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(
        impl: MemoryRepositoryImpl,
    ): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindOverlayRepository(
        impl: OverlayRepositoryImpl,
    ): OverlayRepository

    @Binds
    @Singleton
    abstract fun bindThermalRepository(
        impl: ThermalRepositoryImpl,
    ): ThermalRepository

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        impl: BatteryRepositoryImpl,
    ): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        impl: StorageRepositoryImpl,
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindExportRepository(
        impl: ExportRepositoryImpl,
    ): ExportRepository

    @Binds
    @Singleton
    abstract fun bindProcessRepository(
        impl: ProcessRepositoryImpl,
    ): ProcessRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl,
    ): AlertRepository

    @Binds
    @Singleton
    abstract fun bindBenchmarkRepository(
        impl: BenchmarkRepositoryImpl,
    ): BenchmarkRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl,
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl,
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindAppUpdateRepository(
        impl: AppUpdateRepositoryImpl,
    ): AppUpdateRepository
}
