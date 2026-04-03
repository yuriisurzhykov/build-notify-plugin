package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository

import kotlinx.coroutines.flow.Flow
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

interface IActiveBuildRepository {

    fun observeBuilds(): Flow<List<BuildSnapshot>>

    fun observeLogs(buildId: String): Flow<List<BuildLogEntry>>
}
