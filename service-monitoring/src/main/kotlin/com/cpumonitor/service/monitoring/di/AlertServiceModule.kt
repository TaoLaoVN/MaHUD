package com.cpumonitor.service.monitoring.di

import com.cpumonitor.domain.gateway.AlertNotificationController
import com.cpumonitor.service.monitoring.AlertNotificationControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertServiceModule {

    @Binds
    @Singleton
    abstract fun bindAlertNotificationController(
        impl: AlertNotificationControllerImpl,
    ): AlertNotificationController
}
