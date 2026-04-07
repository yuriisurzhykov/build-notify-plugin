package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession

/**
 * Folds build-related [WsPayload] messages from [session] into a live list of [BuildSnapshot]
 * via [BuildEvent.foldBuilds]. Non-build payloads are dropped by [mapper].
 */
class RemoteActiveBuildSource(
    private val session: ActiveSession,
    private val mapper: Mapper<WsPayload, BuildEvent?>,
    dispatchers: AppDispatchers,
    scope: CoroutineScope,
) : ReadableDataSource<Unit, List<BuildSnapshot>> {

    private val builds: StateFlow<List<BuildSnapshot>> =
        session.incoming
            .mapNotNull { mapper.map(it) }
            .runningFold(emptyMap<String, BuildSnapshot>()) { acc, event ->
                event.foldBuilds(acc)
            }
            .map { it.values.toList() }
            .flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun observe(params: Unit): Flow<List<BuildSnapshot>> = builds
}
