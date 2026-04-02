package me.yuriisoft.buildnotify.mobile.network.pairing

import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost

/**
 * Finite state machine for the PIN-based pairing flow.
 *
 * ```
 * [*] → Idle
 * Idle → AwaitingConfirmation  (startPairing called, PIN computed)
 * AwaitingConfirmation → Confirmed  (user confirms PIN match)
 * AwaitingConfirmation → Rejected   (user rejects or cancels)
 * Confirmed → Idle                  (reset after reconnection succeeds)
 * Rejected  → Idle                  (reset before a new pairing attempt)
 * ```
 *
 * Observed by the UI layer (DiscoveryViewModel / future ConnectionOrchestrator)
 * via [PairingCoordinator.state].
 */
sealed interface PairingState {

    data object Idle : PairingState

    /**
     * Both sides have independently computed the same 6-digit PIN.
     * The user must visually compare the PIN shown on the phone with the
     * one displayed in the IDE plugin's pairing dialog.
     *
     * @param pin          the 6-digit zero-padded PIN string.
     * @param serverName   human-readable server name for display (from [DiscoveredHost.name]).
     * @param host         the host being paired — carried for the [PairingCoordinator]
     *                     to pin the fingerprint on confirmation without mutable fields.
     * @param serverFingerprint the server certificate fingerprint to persist on confirmation.
     */
    data class AwaitingConfirmation(
        val pin: String,
        val serverName: String,
        val host: DiscoveredHost,
        val serverFingerprint: String,
    ) : PairingState

    data object Confirmed : PairingState

    data object Rejected : PairingState
}
