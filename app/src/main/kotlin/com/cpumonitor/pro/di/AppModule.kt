package com.cpumonitor.pro.di

import com.cpumonitor.core.common.dispatcher.DefaultDispatchersProvider
import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.domain.provider.AppVersionProvider
import com.cpumonitor.domain.provider.GitHubReleaseConfig
import com.cpumonitor.pro.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatchersProvider(): DispatchersProvider = DefaultDispatchersProvider()

    @Provides
    @Singleton
    fun provideAppVersionProvider(): AppVersionProvider = object : AppVersionProvider {
        override val versionCode: Int = BuildConfig.VERSION_CODE
        override val versionName: String = BuildConfig.VERSION_NAME
    }

    @Provides
    @Singleton
    fun provideGitHubReleaseConfig(): GitHubReleaseConfig = object : GitHubReleaseConfig {
        override val owner: String = BuildConfig.GITHUB_REPO_OWNER
        override val repo: String = BuildConfig.GITHUB_REPO_NAME
    }
}
