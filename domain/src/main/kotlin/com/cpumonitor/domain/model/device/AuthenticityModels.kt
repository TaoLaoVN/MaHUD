package com.cpumonitor.domain.model.device

/**
 * Overall trust level derived from cross-checking multiple hardware signals.
 */
enum class AuthenticityLevel {
    TRUSTED,
    WARNING,
    SUSPICIOUS,
}

enum class AuthenticitySeverity {
    INFO,
    WARNING,
    CRITICAL,
}

/**
 * A single authenticity signal produced by cross-validation heuristics.
 */
data class AuthenticityFlag(
    val id: String,
    val title: String,
    val detail: String,
    val severity: AuthenticitySeverity,
)

/**
 * Result of validating whether reported device specs look internally consistent.
 */
data class AuthenticityReport(
    val level: AuthenticityLevel,
    val scorePercent: Int,
    val summary: String,
    val flags: List<AuthenticityFlag>,
)
