package com.cpumonitor.domain.usecase.update

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.update.AppUpdateStatus
import com.cpumonitor.domain.model.update.DownloadedUpdate
import com.cpumonitor.domain.provider.AppVersionProvider
import com.cpumonitor.domain.repository.AppUpdateRepository
import com.cpumonitor.domain.usecase.NoParamsUseCase
import com.cpumonitor.domain.usecase.UseCase
import javax.inject.Inject

class CheckForAppUpdateUseCase @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
    private val appVersionProvider: AppVersionProvider,
) : NoParamsUseCase<AppUpdateStatus>() {

    override suspend fun execute(): Result<AppUpdateStatus> =
        appUpdateRepository.checkForUpdate(
            currentVersionCode = appVersionProvider.versionCode,
            currentVersionName = appVersionProvider.versionName,
        )
}

data class DownloadAppUpdateParams(
    val downloadUrl: String,
    val versionName: String,
    val onProgress: (Float) -> Unit = {},
)

class DownloadAppUpdateUseCase @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository,
) : UseCase<DownloadAppUpdateParams, DownloadedUpdate>() {

    override suspend fun execute(params: DownloadAppUpdateParams): Result<DownloadedUpdate> =
        appUpdateRepository.downloadUpdate(
            downloadUrl = params.downloadUrl,
            versionName = params.versionName,
            onProgress = params.onProgress,
        )
}
