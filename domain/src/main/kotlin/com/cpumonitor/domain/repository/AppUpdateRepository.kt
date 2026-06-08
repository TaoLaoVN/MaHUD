package com.cpumonitor.domain.repository

import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.update.AppUpdateStatus
import com.cpumonitor.domain.model.update.DownloadedUpdate

interface AppUpdateRepository {
    suspend fun checkForUpdate(
        currentVersionCode: Int,
        currentVersionName: String,
    ): Result<AppUpdateStatus>

    suspend fun downloadUpdate(
        downloadUrl: String,
        versionName: String,
        onProgress: (Float) -> Unit = {},
    ): Result<DownloadedUpdate>
}
