package com.cpumonitor.data.di

import com.cpumonitor.core.monitoring.FpsMonitorProvider
import com.cpumonitor.core.monitoring.SystemMonitorProvider
import com.cpumonitor.data.monitoring.FpsMonitorProviderImpl
import com.cpumonitor.data.monitoring.SystemMonitorProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings for [SystemMonitorProvider] and proc/system data sources.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringModule {

    @Binds
    @Singleton
    abstract fun bindSystemMonitorProvider(
        impl: SystemMonitorProviderImpl,
    ): SystemMonitorProvider

    @Binds
    @Singleton
    abstract fun bindFpsMonitorProvider(
        impl: FpsMonitorProviderImpl,
    ): FpsMonitorProvider
}
