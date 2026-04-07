package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

data class DiagnosticEvent(
    val buildId: String,
    val severity: DiagnosticSeverity,
    val message: String,
    val filePath: String?,
    val line: Int?,
    val timestamp: Long,
) : BuildEvent {

    override fun foldBuilds(
        builds: Map<String, BuildSnapshot>,
    ): Map<String, BuildSnapshot> = builds

    override fun foldLogs(
        logs: Map<String, List<BuildLogEntry>>,
    ): Map<String, List<BuildLogEntry>> = logs.appendLog(
        buildId = buildId,
        entry = BuildLogEntry(timestamp, message, severity.logKind),
    )
}