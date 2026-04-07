package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

data class BuildOutcome(
    val status: FinishStatus,
    val durationMs: Long,
    val errors: List<BuildIssue>,
    val warnings: List<BuildIssue>,
)
