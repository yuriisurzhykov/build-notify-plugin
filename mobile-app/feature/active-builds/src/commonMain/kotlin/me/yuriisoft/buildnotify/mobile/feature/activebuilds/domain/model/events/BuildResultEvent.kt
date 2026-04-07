package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
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
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> {
        val existing = builds[buildId] ?: return builds
        if (existing is BuildSnapshot.Finished) return builds
        val active = existing as? BuildSnapshot.Active ?: return builds
        return builds + (buildId to BuildSnapshot.Finished(
            buildId = buildId,
            projectName = active.projectName,
            startedAt = active.startedAt,
            outcome = BuildOutcome(
                status = status,
                durationMs = durationMs,
                errors = errors,
                warnings = warnings,
            ),
        ))
    }

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = logs
}