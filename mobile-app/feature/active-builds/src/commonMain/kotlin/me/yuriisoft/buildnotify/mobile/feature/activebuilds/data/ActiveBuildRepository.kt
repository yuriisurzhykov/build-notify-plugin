package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.flow.Flow
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository

class ActiveBuildRepository(
    private val buildsCached: ReadableDataSource<Unit, List<BuildSnapshot>>,
    private val logsCached: ReadableDataSource<String, List<BuildLogEntry>>,
) : IActiveBuildRepository {

    override fun observeBuilds(): Flow<List<BuildSnapshot>> = buildsCached.observe(Unit)

    override fun observeLogs(buildId: String): Flow<List<BuildLogEntry>> =
        logsCached.observe(buildId)
}
