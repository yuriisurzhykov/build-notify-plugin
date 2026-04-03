package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.event

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

data class BuildStartedEvent(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> = builds + (buildId to BuildSnapshot.Active(
        buildId = buildId,
        projectName = projectName,
        startedAt = startedAt,
        currentTask = null,
    ))

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = logs
}
