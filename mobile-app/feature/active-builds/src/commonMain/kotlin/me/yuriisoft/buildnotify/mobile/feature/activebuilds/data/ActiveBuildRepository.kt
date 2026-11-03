package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.flow.Flow
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogStats
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository

/**
 * Reads come from the local store only. The network side
 * ([BuildEventJournal][me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync.BuildEventJournal])
 * writes into that store, so screens never hold or fold the raw event stream.
 */
class ActiveBuildRepository(
    private val builds: ReadableDataSource<Unit, List<BuildSnapshot>>,
    private val logs: ReadableDataSource<LogQuery, List<BuildLogEntry>>,
    private val logStats: ReadableDataSource<String, LogStats>,
) : IActiveBuildRepository {

    override fun observeBuilds(): Flow<List<BuildSnapshot>> = builds.observe(Unit)

    override fun observeLogs(query: LogQuery): Flow<List<BuildLogEntry>> = logs.observe(query)

    override fun observeLogStats(buildId: String): Flow<LogStats> = logStats.observe(buildId)
}
