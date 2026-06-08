package com.cpumonitor.domain.display

/**
 * Maps pixel dimensions to common display marketing classes (720p, 1080p, etc.).
 *
 * Classification uses the shorter screen edge, which matches how phone resolutions
 * are typically advertised (e.g. 1080 x 2400 → Full HD).
 */
object ScreenResolutionClassifier {

    enum class ResolutionClass {
        SD,
        HD,
        FULL_HD,
        QHD,
        UHD_4K,
        UHD_8K,
        UNKNOWN,
    }

    fun classify(widthPixels: Int, heightPixels: Int): ResolutionClass {
        val shortSide = minOf(widthPixels, heightPixels)
        return when {
            shortSide <= 0 -> ResolutionClass.UNKNOWN
            shortSide <= 600 -> ResolutionClass.SD
            shortSide <= 900 -> ResolutionClass.HD
            shortSide <= 1200 -> ResolutionClass.FULL_HD
            shortSide <= 1600 -> ResolutionClass.QHD
            shortSide <= 2400 -> ResolutionClass.UHD_4K
            else -> ResolutionClass.UHD_8K
        }
    }
}
