package com.cpumonitor.domain.model.update

data class AppRelease(
    val tagName: String,
    val versionName: String,
    val versionCode: Int?,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val releasePageUrl: String,
    val publishedAt: String,
)

sealed class AppUpdateStatus {
    data object UpToDate : AppUpdateStatus()

    data class UpdateAvailable(
        val release: AppRelease,
    ) : AppUpdateStatus()

    data class NoReleaseFound(
        val message: String,
    ) : AppUpdateStatus()
}

data class DownloadedUpdate(
    val filePath: String,
    val versionName: String,
)
