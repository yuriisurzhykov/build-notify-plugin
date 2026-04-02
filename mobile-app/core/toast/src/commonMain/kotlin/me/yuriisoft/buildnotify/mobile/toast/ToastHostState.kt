package me.yuriisoft.buildnotify.mobile.toast

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastResult

/**
 * Priority-aware toast queue exposing the currently visible toast as
 * observable Compose state.
 *
 * Internally modeled as a unidirectional command pipeline:
 * `commands → pure state reduction → side-effect execution`.
 * All transitions run on a single collecting coroutine, eliminating the
 * need for a [kotlinx.coroutines.sync.Mutex] or any explicit synchronization.
 *
 * @param scope drives the command pipeline and auto-dismiss timers.
 *   Override in tests with a `TestScope`.
 */
@Stable
class ToastHostState(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {

    private val commands = Channel<ToastCommand>(Channel.UNLIMITED)

    var currentToast: ToastData? by mutableStateOf(null)
        private set

    init {
        val executor = EffectExecutor(scope, commands)
        commands
            .receiveAsFlow()
            .scan(ToastTransition(ToastSnapshot.Empty)) { (prev, _), command ->
                command.reduce(prev)
            }
            .onEach { (snapshot, sideEffects) ->
                sideEffects.forEach { it.execute(executor) }
                currentToast = snapshot.activeToast
            }
            .launchIn(scope)
    }

    /**
     * Enqueues or immediately displays [data] and suspends until the toast
     * is dismissed, timed out, or replaced by a higher-priority toast.
     */
    suspend fun show(data: ToastData): ToastResult {
        val ticket = ToastTicket(data)
        commands.send(ToastCommand.Enqueue(ticket))
        return try {
            ticket.awaitResult()
        } finally {
            withContext(NonCancellable) {
                commands.send(ToastCommand.Evict(ticket))
            }
        }
    }

    /**
     * Dismisses the toast identified by [id], or the currently visible
     * toast when [id] is `null`.
     */
    fun dismiss(id: String? = null) {
        commands.trySend(ToastCommand.Dismiss(id))
    }
}

