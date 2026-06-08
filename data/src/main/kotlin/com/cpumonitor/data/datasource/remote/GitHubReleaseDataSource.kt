package com.cpumonitor.data.datasource.remote

import com.cpumonitor.domain.model.DomainException
import com.cpumonitor.domain.model.update.AppRelease
import com.cpumonitor.domain.provider.GitHubReleaseConfig
import com.cpumonitor.domain.update.VersionComparator
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubReleaseDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gitHubReleaseConfig: GitHubReleaseConfig,
) {

    fun fetchLatestRelease(): AppRelease {
        val request = Request.Builder()
            .url(
                "https://api.github.com/repos/" +
                    "${gitHubReleaseConfig.owner}/${gitHubReleaseConfig.repo}/releases/latest",
            )
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "MaHUD-Android")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                if (response.code == 404) {
                    throw DomainException("No GitHub release found yet.")
                }
                throw DomainException("GitHub API error (${response.code}): $body")
            }
            return parseRelease(JSONObject(body))
        }
    }

    fun downloadFile(
        url: String,
        destination: java.io.File,
        onProgress: (Float) -> Unit,
    ) {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/octet-stream")
            .header("User-Agent", "MaHUD-Android")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw DomainException("Download failed (${response.code})")
            }
            val responseBody = response.body ?: throw DomainException("Empty download response")
            val totalBytes = responseBody.contentLength()
            destination.parentFile?.mkdirs()
            destination.outputStream().use { output ->
                responseBody.byteStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0L) {
                            onProgress((downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                        }
                    }
                }
            }
            onProgress(1f)
        }
    }

    private fun parseRelease(json: JSONObject): AppRelease {
        val tagName = json.optString("tag_name")
        val versionName = VersionComparator.normalizeTag(tagName)
        val releaseNotes = json.optString("body").trim()
        val releasePageUrl = json.optString("html_url")
        val publishedAt = json.optString("published_at")
        val versionCode = extractVersionCode(releaseNotes)
        val apkDownloadUrl = findApkAssetUrl(json.optJSONArray("assets"))
            ?: throw DomainException("Release $tagName has no APK asset attached.")

        return AppRelease(
            tagName = tagName,
            versionName = versionName,
            versionCode = versionCode,
            releaseNotes = releaseNotes,
            apkDownloadUrl = apkDownloadUrl,
            releasePageUrl = releasePageUrl,
            publishedAt = publishedAt,
        )
    }

    private fun findApkAssetUrl(assets: JSONArray?): String? {
        if (assets == null) return null
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            val name = asset.optString("name")
            if (name.endsWith(".apk", ignoreCase = true)) {
                return asset.optString("browser_download_url").takeIf { it.isNotBlank() }
            }
        }
        return null
    }

    private fun extractVersionCode(releaseNotes: String): Int? {
        val patterns = listOf(
            Regex("""versionCode\s*[:=]\s*(\d+)""", RegexOption.IGNORE_CASE),
            Regex("""version_code\s*[:=]\s*(\d+)""", RegexOption.IGNORE_CASE),
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(releaseNotes)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }
}
