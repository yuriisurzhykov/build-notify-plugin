package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession

/**
 * Folds log-producing [WsPayload] messages from [session] into a map of [BuildLogEntry] lists
 * keyed by build id via [BuildEvent.foldLogs]. [observe] exposes logs for a single build.
 */
class RemoteBuildLogSource(
    private val session: ActiveSession,
    private val mapper: Mapper<WsPayload, BuildEvent?>,
    dispatchers: AppDispatchers,
    scope: CoroutineScope,
) : ReadableDataSource<String, List<BuildLogEntry>> {

    private val allLogs: StateFlow<Map<String, List<BuildLogEntry>>> =
        session.incoming
            .mapNotNull { mapper.map(it) }
            .runningFold(emptyMap<String, List<BuildLogEntry>>()) { acc, event ->
                event.foldLogs(acc)
            }
            .flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override fun observe(params: String): Flow<List<BuildLogEntry>> =
        allLogs
            .map { it[params].orEmpty() }
            .distinctUntilChanged()
}
