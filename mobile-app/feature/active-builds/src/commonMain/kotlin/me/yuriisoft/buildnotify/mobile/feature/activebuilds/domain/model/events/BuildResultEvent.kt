package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus

data class BuildResultEvent(
    val buildId: String,
    val projectName: String,
    val status: FinishStatus,
    val durationMs: Long,
    val startedAt: Long,
    val finishedAt: Long,
    val errors: List<BuildIssue>,
    val warnings: List<BuildIssue>,
) : BuildEvent
