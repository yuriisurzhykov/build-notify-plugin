package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.toast.ToastHostState
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData

/**
 * Reactive pipeline that turns [ConnectionState] transitions into toast
 * and navigation side-effects.
 *
 * Read aloud: *"Take each distinct connection state, pair it with the
 * previous one, map the pair to toast actions, execute every action."*
 *
 * All mutation is confined to [ToastHostState] and [Navigator]; the
 * pipeline itself is stateless after construction.
 */
class NetworkStatusPipeline(
    private val connectionState: StateFlow<ConnectionState>,
    private val toastHostState: ToastHostState,
    private val navigator: Navigator,
    private val scope: CoroutineScope,
) : ToastActionExecutor {

    override fun show(data: ToastData) {
        scope.launch { toastHostState.show(data) }
    }

    override fun dismiss(id: String) {
        toastHostState.dismiss(id)
    }

    override fun navigateToDiscovery() {
        navigator.navigateTo(DISCOVERY_ROUTE, clearBackStack = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun activate() {
        connectionState
            .runningFold(TransitionWindow()) { window, state -> window.advance(state) }
            .mapNotNull(TransitionWindow::asPair)
            .flatMapConcat { (prev, curr) ->
                ConnectionToastMapper.mapTransition(prev, curr).asFlow()
            }
            .onEach { action -> action.execute(this) }
            .collect()
    }

    private companion object {
        const val DISCOVERY_ROUTE = "discovery"
    }
}

/**
 * Immutable sliding window used by [runningFold] to pair the current
 * [ConnectionState] with the previous one, enabling transition detection
 * without a mutable accumulator variable.
 */
private data class TransitionWindow(
    val previous: ConnectionState? = null,
    val current: ConnectionState? = null,
) {
    fun advance(next: ConnectionState) = copy(previous = current, current = next)

    fun asPair(): Pair<ConnectionState, ConnectionState>? =
        if (previous != null && current != null) previous to current else null
}
