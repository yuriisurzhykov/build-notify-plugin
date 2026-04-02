package me.yuriisoft.buildnotify.mobile.network.pairing

import kotlinx.coroutines.flow.StateFlow
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.network.tls.ClientIdentityProvider
import me.yuriisoft.buildnotify.mobile.network.tls.TrustedServers

/**
 * Orchestrates the PIN-based mutual-verification pairing flow between the
 * mobile app and an IDE plugin server.
 *
 * ### Protocol overview
 *
 * 1. The first TLS handshake to an unknown server **fails** (the server has
 *    not yet trusted this client).
 * 2. Both sides independently compute a 6-digit PIN from
 *    `SHA-256(serverFingerprint + clientFingerprint) mod 1 000 000`.
 * 3. The user visually compares the PINs displayed on both devices.
 * 4. On confirmation the mobile app pins the server fingerprint in
 *    [TrustedServers]; the IDE user approves the client in the plugin dialog.
 * 5. The next TLS handshake succeeds because both sides now trust each other.
 *
 * ### Responsibilities (SRP)
 *
 * - Decide whether pairing is required for a given host ([isPairingRequired]).
 * - Compute the PIN and transition to [PairingState.AwaitingConfirmation].
 * - Persist the trust decision on user confirmation ([confirm]).
 * - Expose the pairing state machine as a [StateFlow] for the UI / orchestrator.
 *
 * This class intentionally does **not** own the transport or reconnection loop.
 * Those concerns belong to [ConnectionOrchestrator]. The coordinator signals
 * readiness to reconnect
 * by transitioning to [PairingState.Confirmed]; the caller observes this
 * and initiates the reconnection.
 *
 * ### Thread safety
 *
 * All mutable state lives inside a [StateCommunication] backed by
 * [MutableStateFlow], which is thread-safe. The pairing context (host,
 * fingerprint) is carried inside [PairingState.AwaitingConfirmation] rather
 * than in separate mutable fields, so [confirm] reads an atomic snapshot.
 *
 * @param pinCalculator computes the 6-digit PIN from certificate fingerprints.
 * @param trustedServers persistent store for TOFU-pinned server fingerprints.
 * @param clientIdentity provides the SHA-256 fingerprint of this device's
 *   client certificate, used together with the server fingerprint to derive
 *   the PIN.
 */
class PairingCoordinator(
    private val pinCalculator: PinCalculator,
    private val trustedServers: TrustedServers,
    private val clientIdentity: ClientIdentityProvider,
) {

    private val _state: StateCommunication.Mutable<PairingState> =
        StateCommunication(PairingState.Idle as PairingState)

    val state: StateFlow<PairingState> = _state.observe

    /**
     * Returns `true` when [host] requires PIN-based pairing before a secure
     * connection can be established.
     *
     * A host needs pairing when it advertises TLS (`host.isSecure`) and has
     * not yet been pinned in [TrustedServers].
     */
    fun isPairingRequired(host: DiscoveredHost): Boolean =
        host.isSecure && !trustedServers.isPinned(host.trustKey)

    /**
     * Initiates a new pairing attempt.
     *
     * Computes the PIN from the combination of [serverFingerprint] and this
     * device's client certificate fingerprint (obtained from [clientIdentity]),
     * then transitions to [PairingState.AwaitingConfirmation].
     *
     * @param host               the server being paired.
     * @param serverFingerprint  SHA-256 fingerprint of the server certificate,
     *                           captured from the (failed) TLS handshake.
     */
    fun startPairing(
        host: DiscoveredHost,
        serverFingerprint: String,
    ) {
        val pin = pinCalculator.derivePin(serverFingerprint, clientIdentity.fingerprint())
        _state.put(
            PairingState.AwaitingConfirmation(
                pin = pin,
                serverName = host.name,
                host = host,
                serverFingerprint = serverFingerprint,
            ),
        )
    }

    /**
     * Called when the user confirms that the PINs match.
     *
     * Pins the server fingerprint in [TrustedServers] so that subsequent TLS
     * handshakes succeed, then transitions to [PairingState.Confirmed].
     *
     * No-op if the current state is not [PairingState.AwaitingConfirmation].
     */
    fun confirm() {
        val awaiting = _state.observe.value as? PairingState.AwaitingConfirmation ?: return
        trustedServers.pin(awaiting.host.trustKey, awaiting.serverFingerprint)
        _state.put(PairingState.Confirmed)
    }

    /**
     * Called when the user rejects the pairing (PINs do not match, or the user
     * cancels). Transitions to [PairingState.Rejected] without persisting trust.
     *
     * No-op if not currently awaiting confirmation.
     */
    fun reject() {
        if (_state.observe.value !is PairingState.AwaitingConfirmation) return
        _state.put(PairingState.Rejected)
    }

    /**
     * Removes the TOFU pin for [host] from [TrustedServers].
     *
     * Called by [ConnectionOrchestrator] when the server explicitly rejects
     * this client's certificate ([ConnectionErrorReason.ClientRejected]).
     * Without this, [isPairingRequired] would return `false` on the next
     * connection attempt (the server is still "pinned"), and the pairing
     * gate would be skipped — leading to an infinite rejection loop.
     */
    fun unpinServer(host: DiscoveredHost) {
        trustedServers.unpin(host.trustKey)
    }

    /**
     * Resets the coordinator to [PairingState.Idle], ready for a new pairing
     * attempt. Typically called after the caller has acted on [PairingState.Confirmed]
     * (reconnection succeeded) or [PairingState.Rejected] (UI returned to discovery).
     */
    fun reset() {
        _state.put(PairingState.Idle)
    }
}
