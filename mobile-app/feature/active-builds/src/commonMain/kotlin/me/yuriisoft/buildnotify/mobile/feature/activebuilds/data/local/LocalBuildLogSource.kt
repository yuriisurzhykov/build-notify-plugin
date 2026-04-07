package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.BuildLogQueries
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildLogRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

/**
 * Append-only log cache per [buildId] — Scenario C from `core:cache` README.
 * Works directly with [BuildLogQueries] DAO; no intermediate abstraction.
 *
 * [save] is append-only: it counts existing rows for [params] and inserts only the
 * tail of [data] that has not yet been persisted. This is safe because log entries
 * are ordered and never modified after insertion.
 */
class LocalBuildLogSource(
    private val queries: BuildLogQueries,
    private val logMapper: Mapper<BuildLogRecord, BuildLogEntry>,
    private val dispatchers: AppDispatchers,
) : MutableDataSource<String, List<BuildLogEntry>> {

    override fun observe(params: String): Flow<List<BuildLogEntry>> =
        queries.selectByBuildId(params)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows ->
                rows.map { row ->
                    logMapper.map(
                        BuildLogRecord(
                            buildId = row.build_id,
                            timestamp = row.timestamp,
                            message = row.message,
                            kind = LogRecordKind.valueOf(row.kind),
                        ),
                    )
                }
            }

    override suspend fun save(params: String, data: List<BuildLogEntry>) {
        dispatchers.withBackground {
            val existingCount = queries.logCount(params).executeAsOne().toInt()
            val newEntries = data.drop(existingCount)
            if (newEntries.isNotEmpty()) {
                queries.transaction {
                    newEntries.forEach { entry ->
                        queries.insert(
                            build_id = params,
                            timestamp = entry.timestamp,
                            message = entry.message,
                            kind = entry.kind.toRecordKind().name,
                        )
                    }
                }
            }
        }
    }

    override suspend fun delete(params: String) {
        dispatchers.withBackground {
            queries.deleteByBuildId(params)
        }
    }
}

private fun LogKind.toRecordKind(): LogRecordKind = when (this) {
    LogKind.TASK    -> LogRecordKind.TASK
    LogKind.WARNING -> LogRecordKind.WARNING
    LogKind.ERROR   -> LogRecordKind.ERROR
}
