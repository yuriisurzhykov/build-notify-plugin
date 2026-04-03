package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BuildOutcome(
    val status: FinishStatus,
    val durationMs: Long,
    val errors: List<BuildIssue>,
    val warnings: List<BuildIssue>,
)
