package com.cpumonitor.domain.model

/**
 * Realtime internal storage snapshot.
 */
data class StorageMetrics(
    val timestampMillis: Long,
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val categories: List<StorageCategoryUsage>,
)

/**
 * Storage usage attributed to a content category.
 */
data class StorageCategoryUsage(
    val category: StorageCategory,
    val usedBytes: Long,
)

enum class StorageCategory(val displayName: String) {
    APPLICATIONS("Applications"),
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    DOWNLOADS("Downloads"),
    OTHER("Other"),
}
