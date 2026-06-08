package com.cpumonitor.data.datasource.system

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.cpumonitor.data.datasource.SystemDataSource
import com.cpumonitor.domain.model.StorageCategory
import com.cpumonitor.domain.model.StorageCategoryUsage
import com.cpumonitor.domain.model.StorageMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads internal storage totals and category breakdowns from StatFs, MediaStore, and filesystem scans.
 */
@Singleton
class StorageDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemDataSource {

    fun readStorageMetrics(): StorageMetrics {
        val dataDirectory = Environment.getDataDirectory()
        val statFs = StatFs(dataDirectory.path)
        val totalBytes = statFs.blockSizeLong * statFs.blockCountLong
        val freeBytes = statFs.blockSizeLong * statFs.availableBlocksLong
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

        val categories = buildCategoryBreakdown()

        return StorageMetrics(
            timestampMillis = System.currentTimeMillis(),
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
            categories = categories,
        )
    }

    private fun buildCategoryBreakdown(): List<StorageCategoryUsage> {
        val applications = estimateApplicationsBytes()
        val images = queryMediaCollectionSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val videos = queryMediaCollectionSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        val audio = queryMediaCollectionSize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        val documents = directorySize(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        )
        val downloads = directorySize(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        )

        val categorized = listOf(
            StorageCategoryUsage(StorageCategory.APPLICATIONS, applications),
            StorageCategoryUsage(StorageCategory.IMAGES, images),
            StorageCategoryUsage(StorageCategory.VIDEOS, videos),
            StorageCategoryUsage(StorageCategory.AUDIO, audio),
            StorageCategoryUsage(StorageCategory.DOCUMENTS, documents),
            StorageCategoryUsage(StorageCategory.DOWNLOADS, downloads),
        ).filter { it.usedBytes > 0L }

        return categorized.ifEmpty {
            listOf(StorageCategoryUsage(StorageCategory.OTHER, 0L))
        }
    }

    private fun estimateApplicationsBytes(): Long {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(0)
            .filter { appInfo -> (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sumOf { appInfo -> apkSizeBytes(appInfo) }
    }

    private fun apkSizeBytes(appInfo: ApplicationInfo): Long {
        var total = File(appInfo.sourceDir).length()
        appInfo.splitSourceDirs?.forEach { splitPath ->
            total += File(splitPath).length()
        }
        return total
    }

    private fun queryMediaCollectionSize(collectionUri: android.net.Uri): Long {
        var total = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        context.contentResolver.query(collectionUri, projection, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            if (sizeIndex < 0) return@use
            while (cursor.moveToNext()) {
                if (!cursor.isNull(sizeIndex)) {
                    total += cursor.getLong(sizeIndex)
                }
            }
        }
        return total.coerceAtLeast(0L)
    }

    private fun directorySize(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        return directory.walkTopDown()
            .filter { it.isFile }
            .sumOf { file -> file.length() }
            .coerceAtLeast(0L)
    }
}
