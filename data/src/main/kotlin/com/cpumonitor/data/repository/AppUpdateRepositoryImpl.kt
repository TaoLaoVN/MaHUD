package com.cpumonitor.data.repository

import android.content.Context
import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import com.cpumonitor.data.datasource.remote.GitHubReleaseDataSource
import com.cpumonitor.domain.model.DomainException
import com.cpumonitor.domain.model.Result
import com.cpumonitor.domain.model.update.AppUpdateStatus
import com.cpumonitor.domain.model.update.DownloadedUpdate
import com.cpumonitor.domain.repository.AppUpdateRepository
import com.cpumonitor.domain.update.VersionComparator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
    private val gitHubReleaseDataSource: GitHubReleaseDataSource,
) : AppUpdateRepository {

    override suspend fun checkForUpdate(
        currentVersionCode: Int,
        currentVersionName: String,
    ): Result<AppUpdateStatus> = withContext(dispatchersProvider.io) {
        runCatching {
            val latestRelease = gitHubReleaseDataSource.fetchLatestRelease()
            val hasUpdate = VersionComparator.isNewerVersion(
                remoteVersionName = latestRelease.versionName,
                remoteVersionCode = latestRelease.versionCode,
                currentVersionName = currentVersionName,
                currentVersionCode = currentVersionCode,
            )
            if (hasUpdate) {
                AppUpdateStatus.UpdateAvailable(latestRelease)
            } else {
                AppUpdateStatus.UpToDate
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { throwable ->
                when (throwable) {
                    is DomainException -> {
                        val message = throwable.message
                        if (message == "No GitHub release found yet.") {
                            Result.Success(
                                AppUpdateStatus.NoReleaseFound(
                                    message = message,
                                ),
                            )
                        } else {
                            Result.Error(throwable)
                        }
                    }

                    else -> Result.Error(
                        DomainException(
                            message = throwable.message ?: "Failed to check for updates",
                            cause = throwable,
                        ),
                    )
                }
            },
        )
    }

    override suspend fun downloadUpdate(
        downloadUrl: String,
        versionName: String,
        onProgress: (Float) -> Unit,
    ): Result<DownloadedUpdate> = withContext(dispatchersProvider.io) {
        runCatching {
            val destination = File(context.cacheDir, "updates/mahud-$versionName.apk")
            gitHubReleaseDataSource.downloadFile(
                url = downloadUrl,
                destination = destination,
                onProgress = onProgress,
            )
            DownloadedUpdate(
                filePath = destination.absolutePath,
                versionName = versionName,
            )
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { throwable ->
                when (throwable) {
                    is DomainException -> Result.Error(throwable)
                    else -> Result.Error(
                        DomainException(
                            message = throwable.message ?: "Failed to download update",
                            cause = throwable,
                        ),
                    )
                }
            },
        )
    }
}
