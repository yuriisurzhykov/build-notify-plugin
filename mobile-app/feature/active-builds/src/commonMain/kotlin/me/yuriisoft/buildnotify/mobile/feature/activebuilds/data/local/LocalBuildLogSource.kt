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
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.BuildLogQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.toRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildLogRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery

/**
 * A window onto the tail of one build's log: at most [LogQuery.limit] rows are read per
 * change, however long the log grows. Paging back means asking for a larger limit.
 */
class LocalBuildLogSource(
    private val queries: BuildLogQueries,
    private val logMapper: Mapper<BuildLogRecord, BuildLogEntry>,
    private val dispatchers: AppDispatchers,
) : ReadableDataSource<LogQuery, List<BuildLogEntry>> {

    override fun observe(params: LogQuery): Flow<List<BuildLogEntry>> =
        queries.selectTail(
            buildId = params.buildId,
            kinds = params.kinds.map { it.toRecordKind().name },
            limit = params.limit.toLong(),
        )
            .asFlow()
            .mapToList(dispatchers.io)
            .map { newestFirst ->
                newestFirst.asReversed().map { row ->
                    logMapper.map(
                        BuildLogRecord(
                            buildId = row.build_id,
                            timestamp = row.timestamp,
                            message = row.message,
                            kind = LogRecordKind.valueOf(row.kind),
                            id = row.id,
                        ),
                    )
                }
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.io)
}
