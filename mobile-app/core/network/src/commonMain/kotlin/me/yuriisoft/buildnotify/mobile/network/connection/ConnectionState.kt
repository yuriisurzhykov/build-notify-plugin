package me.yuriisoft.buildnotify.mobile.network.connection

import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason

/**
 * Finite state machine for the WebSocket connection lifecycle.
 *
 * Transitions are driven by the Flow pipeline inside [ConnectionOrchestrator]:
 *
 * ```
 * [*] → Idle
 * Idle → Connecting                 (connect called)
 * Connecting → PairingRequired      (first TLS handshake fails, PIN shown)
 * PairingRequired → Connecting      (user confirms PIN, reconnect loop starts)
 * Connecting → Connected            (first payload arrives)
 * Connected → Reconnecting          (error / heartbeat timeout)
 * Reconnecting → Connected          (payload after retry)
 * Reconnecting → Failed             (retries exhausted)
 * Connected → Disconnected          (disconnect called)
 * Connecting → Disconnected         (disconnect called)
 * PairingRequired → Disconnected    (user rejects or disconnect called)
 * Reconnecting → Disconnected       (disconnect called)
 * Failed → Connecting               (connect called again)
 * Disconnected → Connecting         (connect called again)
 * ```
 *
 * Observed via [ConnectionManager.state] by the UI and foreground service.
 */
sealed interface ConnectionState {

    data object Idle : ConnectionState

    data class Connecting(val host: DiscoveredHost) : ConnectionState

    /**
     * The server is not yet trusted and PIN-based pairing is required.
     *
     * Both sides have independently computed the same 6-digit [pin] from
     * `SHA-256(serverFingerprint + clientFingerprint) mod 1 000 000`.
     * The user must visually compare the PIN shown on the phone with the
     * one displayed in the IDE plugin's pairing dialog.
     *
     * The [ConnectionOrchestrator] transitions here when a first-time
     * TLS handshake fails and the [PairingCoordinator] indicates that
     * pairing is required. On user confirmation the orchestrator moves
     * back to [Connecting] and starts the reconnection loop.
     */
    data class PairingRequired(
        val host: DiscoveredHost,
        val pin: String,
    ) : ConnectionState

    data class Connected(val host: DiscoveredHost) : ConnectionState

    data class Reconnecting(
        val host: DiscoveredHost,
        val attempt: Long,
    ) : ConnectionState

    data object Disconnected : ConnectionState

    data class Failed(
        val host: DiscoveredHost,
        val reason: ConnectionErrorReason,
    ) : ConnectionState
}
