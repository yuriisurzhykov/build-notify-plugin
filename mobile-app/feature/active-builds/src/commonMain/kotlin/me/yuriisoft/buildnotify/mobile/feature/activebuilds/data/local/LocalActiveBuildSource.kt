package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.ActiveBuildQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.Active_build
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.BuildIssueQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.Build_issue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

/**
 * Read side of the build list: two queries per change (builds, then all their issues),
 * never one query per build. Writes go through [SqlBuildEventStore].
 */
class LocalActiveBuildSource(
    private val activeBuildQueries: ActiveBuildQueries,
    private val issueQueries: BuildIssueQueries,
    private val snapshotMapper: Mapper<Pair<BuildRecord, List<BuildIssueRecord>>, BuildSnapshot>,
    private val dispatchers: AppDispatchers,
) : ReadableDataSource<Unit, List<BuildSnapshot>> {

    override fun observe(params: Unit): Flow<List<BuildSnapshot>> =
        activeBuildQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows ->
                val issuesByBuild = issueQueries.selectAll().executeAsList().groupBy(
                    keySelector = { it.build_id },
                    valueTransform = { it.toRecord() },
                )
                rows.map { row -> snapshotFromRow(row, issuesByBuild[row.build_id].orEmpty()) }
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.io)

    private fun snapshotFromRow(row: Active_build, issues: List<BuildIssueRecord>): BuildSnapshot {
        val record = BuildRecord(
            buildId = row.build_id,
            projectName = row.project_name,
            status = BuildRecordStatus.valueOf(row.status),
            startedAt = row.started_at,
            currentTask = row.current_task,
            durationMs = row.duration_ms,
        )
        return snapshotMapper.map(record to issues)
    }
}

private fun Build_issue.toRecord(): BuildIssueRecord = BuildIssueRecord(
    buildId = build_id,
    message = message,
    filePath = file_path,
    line = line?.toInt(),
    severity = IssueSeverity.valueOf(severity),
)
