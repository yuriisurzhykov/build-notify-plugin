package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.event

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

data class SnapshotEvent(
    val activeBuilds: List<SnapshotBuild>,
    val recentResults: List<BuildResultEvent>,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> {
        val active: Map<String, BuildSnapshot> =
            activeBuilds.associate { it.buildId to it.toSnapshot() }
        return recentResults.fold(active) { acc, result ->
            result.foldBuilds(acc)
        }
    }

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = emptyMap()
}

private fun SnapshotBuild.toSnapshot(): BuildSnapshot.Active = BuildSnapshot.Active(
    buildId = buildId,
    projectName = projectName,
    startedAt = startedAt,
    currentTask = currentTask,
)
