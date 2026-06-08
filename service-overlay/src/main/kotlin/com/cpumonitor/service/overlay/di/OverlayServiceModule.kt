package com.cpumonitor.service.overlay.di

import com.cpumonitor.domain.gateway.OverlayServiceController
import com.cpumonitor.service.overlay.OverlayServiceControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayServiceModule {

    @Binds
    @Singleton
    abstract fun bindOverlayServiceController(
        impl: OverlayServiceControllerImpl,
    ): OverlayServiceController
}
