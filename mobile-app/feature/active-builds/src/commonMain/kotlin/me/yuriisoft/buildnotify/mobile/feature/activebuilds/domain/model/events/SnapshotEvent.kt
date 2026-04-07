package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

data class SnapshotEvent(
    val activeBuilds: List<SnapshotBuild>,
    val recentResults: List<BuildResultEvent>,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> {
        val active: Map<String, BuildSnapshot> =
            activeBuilds.associate { it.buildId to it.toActiveSnapshot() }
        val finished: Map<String, BuildSnapshot> =
            recentResults.associate { it.buildId to it.toFinishedSnapshot() }
        return active + finished
    }

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = emptyMap()
}


private fun SnapshotBuild.toActiveSnapshot(): BuildSnapshot.Active = BuildSnapshot.Active(
    buildId = buildId,
    projectName = projectName,
    startedAt = startedAt,
    currentTask = currentTask,
)

private fun BuildResultEvent.toFinishedSnapshot(): BuildSnapshot.Finished = BuildSnapshot.Finished(
    buildId = buildId,
    projectName = projectName,
    startedAt = startedAt,
    outcome = BuildOutcome(
        status = status,
        durationMs = durationMs,
        errors = errors,
        warnings = warnings,
    ),
)