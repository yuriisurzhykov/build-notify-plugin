package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.CoroutineScope
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.BuildEventStore
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionManager
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState

/**
 * Drops builds still marked as running when the connection enters a terminal state
 * ([ConnectionState.Disconnected] or [ConnectionState.Failed]): their results can no
 * longer arrive, so they would spin forever. Finished builds and their logs are kept,
 * so the history stays readable offline.
 *
 * Nothing is dropped on [ConnectionState.Reconnecting]: running builds stay visible
 * while the connection is being restored, and the IDE's snapshot reconciles them.
 */
class BuildCacheInvalidator(
    connectionManager: ConnectionManager,
    private val store: BuildEventStore,
    dispatchers: AppDispatchers,
    scope: CoroutineScope,
) {
    init {
        dispatchers.launchBackground(scope) {
            var previous: ConnectionState? = null
            connectionManager.state.collect { current ->
                val isTerminal = current is ConnectionState.Disconnected
                    || current is ConnectionState.Failed
                val wasTerminal = previous is ConnectionState.Disconnected
                    || previous is ConnectionState.Failed
                if (previous != null && isTerminal && !wasTerminal) {
                    store.dropActiveBuilds()
                }
                previous = current
            }
        }
    }
}
