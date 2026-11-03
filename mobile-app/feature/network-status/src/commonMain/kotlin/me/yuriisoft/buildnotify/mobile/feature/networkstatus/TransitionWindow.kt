package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import kotlinx.coroutines.flow.runningFold
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState

/**
 * Immutable sliding window used by [runningFold] to pair the current
 * [ConnectionState] with the previous one, enabling transition detection
 * without a mutable accumulator variable.
 */
internal data class TransitionWindow(
    val previous: ConnectionState? = null,
    val current: ConnectionState? = null,
) {
    fun advance(next: ConnectionState) = copy(previous = current, current = next)

    fun asPair(): Pair<ConnectionState, ConnectionState>? =
        if (previous != null && current != null) previous to current else null
}
