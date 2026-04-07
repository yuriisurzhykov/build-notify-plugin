package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.CoroutineScope
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionManager
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState

/**
 * Clears the local build cache when the connection enters a terminal state
 * ([ConnectionState.Disconnected] or [ConnectionState.Failed]).
 *
 * Calling [MutableDataSource.delete] on [localBuilds] is sufficient: the SQL schema
 * defines `ON DELETE CASCADE` on both `build_issue` and `build_log`, so deleting all
 * `active_build` rows automatically removes all associated issues and log entries.
 *
 * The cache is **not** cleared on [ConnectionState.Reconnecting] — builds remain
 * visible while the system attempts to restore the connection.
 */
class BuildCacheInvalidator(
    connectionManager: ConnectionManager,
    private val localBuilds: MutableDataSource<Unit, List<BuildSnapshot>>,
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
                    localBuilds.delete(Unit)
                }
                previous = current
            }
        }
    }
}
