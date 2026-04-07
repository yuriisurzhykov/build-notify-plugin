package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

sealed interface BuildEvent {

    fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot>

    fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>>
}

internal fun Map<String, List<BuildLogEntry>>.appendLog(
    buildId: String,
    entry: BuildLogEntry,
): Map<String, List<BuildLogEntry>> = this + (buildId to (this[buildId].orEmpty() + entry))

data class SnapshotBuild(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val currentTask: String?,
)
