package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveBuildInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildDiagnosticPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildSnapshotPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStatus
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskFinishedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildResultEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotBuild
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskFinishedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildIssue as WireBuildIssue
import me.yuriisoft.buildnotify.mobile.data.protocol.DiagnosticSeverity as WireSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue as DomainBuildIssue

class BuildEventMapper(
    private val clock: () -> Long,
) : Mapper<WsPayload, BuildEvent?> {

    override fun map(from: WsPayload): BuildEvent? {
        val now = clock()
        return when (from) {
            is BuildStartedPayload    -> BuildStartedEvent(
                buildId = from.buildId,
                projectName = from.projectName,
                startedAt = now,
            )

            is TaskStartedPayload     -> TaskStartedEvent(
                buildId = from.buildId,
                taskPath = from.taskPath,
                timestamp = now,
            )

            is TaskFinishedPayload    -> TaskFinishedEvent(
                buildId = from.buildId,
                taskPath = from.taskPath,
                status = from.status.name,
                timestamp = now,
            )

            is BuildDiagnosticPayload -> DiagnosticEvent(
                buildId = from.buildId,
                severity = from.severity.toDomain(),
                message = from.message,
                filePath = from.filePath,
                line = from.line,
                timestamp = now,
            )

            is BuildResultPayload     -> from.result.toBuildResultEvent()
            is BuildSnapshotPayload   -> SnapshotEvent(
                activeBuilds = from.activeBuilds.map(::toSnapshotBuild),
                recentResults = from.recentResults.map { it.toBuildResultEvent() },
            )

            else                      -> null
        }
    }
}

private fun WireSeverity.toDomain(): DiagnosticSeverity = when (this) {
    WireSeverity.WARNING -> DiagnosticSeverity.WARNING
    WireSeverity.ERROR   -> DiagnosticSeverity.ERROR
}

private fun BuildResult.toBuildResultEvent(): BuildResultEvent = BuildResultEvent(
    buildId = buildId,
    projectName = projectName,
    status = status.toFinishStatus(),
    durationMs = durationMs,
    startedAt = startedAt,
    finishedAt = finishedAt,
    errors = errors.map(WireBuildIssue::toDomain),
    warnings = warnings.map(WireBuildIssue::toDomain),
)

private fun BuildStatus.toFinishStatus(): FinishStatus = when (this) {
    BuildStatus.SUCCESS   -> FinishStatus.SUCCESS
    BuildStatus.FAILED    -> FinishStatus.FAILED
    BuildStatus.CANCELLED -> FinishStatus.CANCELLED
    BuildStatus.STARTED   -> error(
        "BuildResult must carry a terminal status (SUCCESS, FAILED, CANCELLED), " +
                "not STARTED. A STARTED status in a result payload is a server-side bug.",
    )
}

private fun WireBuildIssue.toDomain(): DomainBuildIssue = DomainBuildIssue(
    message = message,
    filePath = filePath,
    line = line,
)

private fun toSnapshotBuild(info: ActiveBuildInfo): SnapshotBuild = SnapshotBuild(
    buildId = info.buildId,
    projectName = info.projectName,
    startedAt = info.startedAt,
    currentTask = info.tasks.lastOrNull { it.status == TaskStatus.RUNNING }?.taskPath,
)
