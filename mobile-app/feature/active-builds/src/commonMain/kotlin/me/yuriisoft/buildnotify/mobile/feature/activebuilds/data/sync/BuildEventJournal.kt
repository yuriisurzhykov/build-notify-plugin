package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.BuildEventStore
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The only consumer of build events: for the whole process lifetime it turns the event
 * stream into batched writes to [store].
 *
 * A burst of task events (a Gradle build emits thousands) becomes a handful of
 * transactions, and everything on screen reads from the store. A failing message or
 * write loses only its own batch; the journal subscribes again and keeps going.
 */
class BuildEventJournal(
    session: ActiveSession,
    mapper: Mapper<WsPayload, BuildEvent?>,
    store: BuildEventStore,
    dispatchers: AppDispatchers,
    scope: CoroutineScope,
    window: Duration = DEFAULT_WINDOW,
    maxBatch: Int = DEFAULT_MAX_BATCH,
) {
    init {
        dispatchers.launchBackground(scope) {
            session.incoming
                .mapNotNull { mapper.map(it) }
                .batched(window, maxBatch)
                .onEach { store.write(it) }
                .retry { error -> error !is CancellationException }
                .collect { }
        }
    }

    private companion object {
        val DEFAULT_WINDOW: Duration = 150.milliseconds
        const val DEFAULT_MAX_BATCH = 500
    }
}
