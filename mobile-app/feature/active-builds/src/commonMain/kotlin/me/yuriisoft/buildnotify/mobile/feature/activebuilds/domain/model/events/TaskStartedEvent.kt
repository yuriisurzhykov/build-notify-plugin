package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

data class TaskStartedEvent(
    val buildId: String,
    val taskPath: String,
    val timestamp: Long,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> {
        val active = builds[buildId] as? BuildSnapshot.Active ?: return builds
        return builds + (buildId to active.copy(currentTask = taskPath))
    }

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = logs.appendLog(
        buildId = buildId,
        entry = BuildLogEntry(timestamp, taskPath, LogKind.TASK),
    )
}