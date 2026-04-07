package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.ActiveBuildQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.Active_build
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.BuildIssueQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

/**
 * SQLDelight-backed local source for active builds — Scenario C from `core:cache` README.
 * Works directly with [ActiveBuildQueries] and [BuildIssueQueries] DAOs; no intermediate
 * abstraction between this class and the storage engine.
 *
 * [save] uses replace-all semantics: stale build rows absent from [data] are deleted
 * (which cascade-removes their issues and logs via SQL), then each snapshot is upserted.
 * [delete] calls [ActiveBuildQueries.deleteAll]; CASCADE removes all issues and logs.
 */
class LocalActiveBuildSource(
    private val activeBuildQueries: ActiveBuildQueries,
    private val issueQueries: BuildIssueQueries,
    private val snapshotMapper: Mapper<Pair<BuildRecord, List<BuildIssueRecord>>, BuildSnapshot>,
    private val dispatchers: AppDispatchers,
) : MutableDataSource<Unit, List<BuildSnapshot>> {

    override fun observe(params: Unit): Flow<List<BuildSnapshot>> =
        activeBuildQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.map { row -> snapshotFromRow(row) } }

    override suspend fun save(params: Unit, data: List<BuildSnapshot>) {
        dispatchers.withBackground {
            activeBuildQueries.transaction {
                val incomingIds = data.map { it.buildId }.toSet()
                activeBuildQueries.selectAll()
                    .executeAsList()
                    .map { it.build_id }
                    .filterNot { it in incomingIds }
                    .forEach { staleId -> activeBuildQueries.deleteById(staleId) }
                data.forEach { snapshot -> persistSnapshot(snapshot) }
            }
        }
    }

    override suspend fun delete(params: Unit) {
        dispatchers.withBackground {
            activeBuildQueries.deleteAll()
        }
    }

    private fun snapshotFromRow(row: Active_build): BuildSnapshot {
        val record = BuildRecord(
            buildId = row.build_id,
            projectName = row.project_name,
            status = BuildRecordStatus.valueOf(row.status),
            startedAt = row.started_at,
            currentTask = row.current_task,
            durationMs = row.duration_ms,
        )
        val issues = issueQueries.selectByBuildId(record.buildId)
            .executeAsList()
            .map { issue ->
                BuildIssueRecord(
                    buildId = issue.build_id,
                    message = issue.message,
                    filePath = issue.file_path,
                    line = issue.line?.toInt(),
                    severity = IssueSeverity.valueOf(issue.severity),
                )
            }
        return snapshotMapper.map(record to issues)
    }

    private fun persistSnapshot(snapshot: BuildSnapshot) {
        when (snapshot) {
            is BuildSnapshot.Active   -> persistActive(snapshot)
            is BuildSnapshot.Finished -> persistFinished(snapshot)
        }
    }

    private fun persistActive(snapshot: BuildSnapshot.Active) {
        issueQueries.deleteByBuildId(snapshot.buildId)
        activeBuildQueries.upsert(
            build_id = snapshot.buildId,
            project_name = snapshot.projectName,
            status = BuildRecordStatus.ACTIVE.name,
            started_at = snapshot.startedAt,
            current_task = snapshot.currentTask,
            duration_ms = null,
        )
    }

    private fun persistFinished(snapshot: BuildSnapshot.Finished) {
        issueQueries.deleteByBuildId(snapshot.buildId)
        activeBuildQueries.upsert(
            build_id = snapshot.buildId,
            project_name = snapshot.projectName,
            status = BuildRecordStatus.fromDomainFinish(snapshot.outcome.status).name,
            started_at = snapshot.startedAt,
            current_task = null,
            duration_ms = snapshot.outcome.durationMs,
        )
        snapshot.outcome.errors.forEach { issue ->
            issueQueries.insert(
                build_id = snapshot.buildId,
                message = issue.message,
                file_path = issue.filePath,
                line = issue.line?.toLong(),
                severity = IssueSeverity.ERROR.name,
            )
        }
        snapshot.outcome.warnings.forEach { issue ->
            issueQueries.insert(
                build_id = snapshot.buildId,
                message = issue.message,
                file_path = issue.filePath,
                line = issue.line?.toLong(),
                severity = IssueSeverity.WARNING.name,
            )
        }
    }
}
