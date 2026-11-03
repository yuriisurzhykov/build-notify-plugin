package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository

import kotlinx.coroutines.flow.Flow
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogStats

interface IActiveBuildRepository {

    fun observeBuilds(): Flow<List<BuildSnapshot>>

    /** A bounded window of a build's log, oldest line first. */
    fun observeLogs(query: LogQuery): Flow<List<BuildLogEntry>>

    fun observeLogStats(buildId: String): Flow<LogStats>
}
