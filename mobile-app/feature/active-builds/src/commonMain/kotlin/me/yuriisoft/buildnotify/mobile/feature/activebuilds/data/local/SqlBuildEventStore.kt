package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.toRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildResultEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskFinishedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent

/**
 * Applies build events straight to SQLite.
 *
 * Every write is O(batch): log lines are appended, the current task and the line
 * counters are a few one-row UPDATEs per batch, a result touches only its own build.
 * Nothing re-reads or rewrites the log, and because a whole batch is one transaction,
 * SQLDelight observers re-query once per batch rather than once per event.
 *
 * Retention: the newest [historyLimit] finished builds are kept with their logs; older
 * ones are deleted together with their lines and issues.
 */
class SqlBuildEventStore(
    private val db: ActiveBuildsDatabase,
    private val dispatchers: AppDispatchers,
    private val historyLimit: Int = HISTORY_LIMIT,
) : BuildEventStore {

    private val builds get() = db.activeBuildQueries
    private val issues get() = db.buildIssueQueries
    private val logs get() = db.buildLogQueries
    private val lineCounts get() = db.buildLogCountQueries

    override suspend fun write(events: List<BuildEvent>) {
        if (events.isEmpty()) return
        dispatchers.withBackground {
            db.transaction {
                val batch = WriteBatch(builds.selectAll().executeAsList().map { it.build_id })
                events.forEach { event -> apply(event, batch) }
                flush(batch)
                pruneHistory()
            }
        }
    }

    override suspend fun dropActiveBuilds() {
        dispatchers.withBackground {
            db.transaction {
                builds.selectActiveIds().executeAsList().forEach(::deleteBuild)
            }
        }
    }

    private fun apply(event: BuildEvent, batch: WriteBatch) {
        when (event) {
            is BuildStartedEvent -> insertActive(event.buildId, event.projectName, event.startedAt, batch)
            is TaskStartedEvent  -> if (batch.isKnown(event.buildId)) {
                batch.taskStarted(event.buildId, event.taskPath)
                appendLog(event.buildId, event.timestamp, event.taskPath, LogRecordKind.TASK, batch)
            }

            is TaskFinishedEvent -> appendLog(
                event.buildId,
                event.timestamp,
                "${event.taskPath} ${event.status}",
                LogRecordKind.TASK,
                batch,
            )

            is DiagnosticEvent   ->
                appendLog(event.buildId, event.timestamp, event.message, event.severity.toRecordKind(), batch)

            is BuildResultEvent  -> applyResult(event, batch)
            is SnapshotEvent     -> applySnapshot(event, batch)
        }
    }

    /** Batched bookkeeping: one UPDATE per build for the task, one per build and kind for counts. */
    private fun flush(batch: WriteBatch) {
        batch.forEachCurrentTask { buildId, task -> builds.updateCurrentTask(task, buildId) }
        batch.forEachLineCount { buildId, kind, count ->
            lineCounts.ensure(buildId = buildId, kind = kind.name, firstTimestamp = count.firstTimestamp)
            lineCounts.add(added = count.lines, buildId = buildId, kind = kind.name)
        }
    }

    private fun insertActive(buildId: String, projectName: String, startedAt: Long, batch: WriteBatch) {
        builds.insertIfAbsent(
            build_id = buildId,
            project_name = projectName,
            status = BuildRecordStatus.ACTIVE.name,
            started_at = startedAt,
            current_task = null,
            duration_ms = null,
        )
        batch.added(buildId)
    }

    /** Lines of builds the store does not know are dropped: they have no row to belong to. */
    private fun appendLog(
        buildId: String,
        timestamp: Long,
        message: String,
        kind: LogRecordKind,
        batch: WriteBatch,
    ) {
        if (!batch.isKnown(buildId)) return
        logs.insert(build_id = buildId, timestamp = timestamp, message = message, kind = kind.name)
        batch.lineAppended(buildId, kind, timestamp)
    }

    private fun applyResult(event: BuildResultEvent, batch: WriteBatch) {
        batch.finished(event.buildId)
        val status = BuildRecordStatus.fromDomainFinish(event.status).name
        when (builds.selectStatus(event.buildId).executeAsOneOrNull()) {
            null                          -> {
                builds.insertIfAbsent(
                    build_id = event.buildId,
                    project_name = event.projectName,
                    status = status,
                    started_at = event.startedAt,
                    current_task = null,
                    duration_ms = event.durationMs,
                )
                batch.added(event.buildId)
                insertIssues(event)
            }

            BuildRecordStatus.ACTIVE.name -> {
                builds.finish(status = status, durationMs = event.durationMs, buildId = event.buildId)
                issues.deleteByBuildId(event.buildId)
                insertIssues(event)
            }

            else                          -> Unit // already finished; results never change
        }
    }

    private fun insertIssues(event: BuildResultEvent) {
        event.errors.forEach { insertIssue(event.buildId, it, IssueSeverity.ERROR) }
        event.warnings.forEach { insertIssue(event.buildId, it, IssueSeverity.WARNING) }
    }

    private fun insertIssue(buildId: String, issue: BuildIssue, severity: IssueSeverity) {
        issues.insert(
            build_id = buildId,
            message = issue.message,
            file_path = issue.filePath,
            line = issue.line?.toLong(),
            severity = severity.name,
        )
    }

    /**
     * The IDE is the authority on what is running: local builds still marked as running
     * but absent from the snapshot are stale and dropped. Finished history is kept.
     */
    private fun applySnapshot(event: SnapshotEvent, batch: WriteBatch) {
        val running = event.activeBuilds.mapTo(HashSet()) { it.buildId }
        builds.selectActiveIds().executeAsList()
            .filterNot { it in running }
            .forEach { stale ->
                deleteBuild(stale)
                batch.removed(stale)
            }
        event.activeBuilds.forEach { build ->
            insertActive(build.buildId, build.projectName, build.startedAt, batch)
            build.currentTask?.let { batch.taskStarted(build.buildId, it) }
        }
        event.recentResults.forEach { applyResult(it, batch) }
    }

    /** Runs after [flush], so nothing is left pending for the builds it deletes. */
    private fun pruneHistory() {
        builds.selectFinishedOverflow(historyLimit.toLong()).executeAsList().forEach(::deleteBuild)
    }

    /** Explicit child deletes, so retention does not depend on the foreign-key pragma. */
    private fun deleteBuild(buildId: String) {
        lineCounts.deleteByBuildId(buildId)
        logs.deleteByBuildId(buildId)
        issues.deleteByBuildId(buildId)
        builds.deleteById(buildId)
    }

    private companion object {
        const val HISTORY_LIMIT = 30
    }
}
