package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.BuildLogCountQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.toDomain
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogStats

/** Line counts per kind of one build's log, read from counters the store keeps; no line is loaded. */
class LocalLogStatsSource(
    private val queries: BuildLogCountQueries,
    private val dispatchers: AppDispatchers,
) : ReadableDataSource<String, LogStats> {

    override fun observe(params: String): Flow<LogStats> =
        queries.selectByBuildId(params)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows ->
                LogStats(
                    countsByKind = rows.associate { row ->
                        LogRecordKind.valueOf(row.kind).toDomain() to row.line_count.toInt()
                    },
                    firstTimestamp = rows.minOfOrNull { it.first_timestamp },
                )
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.io)
}
