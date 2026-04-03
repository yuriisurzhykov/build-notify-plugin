package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.event

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

data class TaskFinishedEvent(
    val buildId: String,
    val taskPath: String,
    val status: String,
    val timestamp: Long,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> = builds

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = logs.appendLog(
        buildId = buildId,
        entry = BuildLogEntry(timestamp, "$taskPath $status", LogKind.TASK),
    )
}
