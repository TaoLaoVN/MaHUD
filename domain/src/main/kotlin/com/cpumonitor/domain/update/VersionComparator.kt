package com.cpumonitor.domain.update

object VersionComparator {

    fun isNewerVersion(
        remoteVersionName: String,
        remoteVersionCode: Int?,
        currentVersionName: String,
        currentVersionCode: Int,
    ): Boolean {
        if (remoteVersionCode != null && remoteVersionCode > currentVersionCode) {
            return true
        }
        if (remoteVersionCode != null && remoteVersionCode < currentVersionCode) {
            return false
        }
        return compareSemVer(remoteVersionName, currentVersionName) > 0
    }

    fun normalizeTag(tagName: String): String =
        tagName.trim().removePrefix("v").removePrefix("V")

    fun compareSemVer(remote: String, current: String): Int {
        val remoteParts = parseVersionParts(remote)
        val currentParts = parseVersionParts(current)
        val maxSize = maxOf(remoteParts.size, currentParts.size)
        for (index in 0 until maxSize) {
            val remotePart = remoteParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (remotePart != currentPart) {
                return remotePart.compareTo(currentPart)
            }
        }
        return 0
    }

    private fun parseVersionParts(version: String): List<Int> {
        val normalized = normalizeTag(version)
        val core = normalized.substringBefore('-').substringBefore('+')
        return core.split('.', '-', '_')
            .mapNotNull { part ->
                part.filter(Char::isDigit).toIntOrNull()
            }
    }
}
