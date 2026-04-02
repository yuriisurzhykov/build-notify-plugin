package me.yuriisoft.buildnotify.mobile.network.connection

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withTimeoutOrNull
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.data.protocol.HelloPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionOrchestrator.Companion.PROBE_TIMEOUT
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import me.yuriisoft.buildnotify.mobile.network.pairing.PairingCoordinator
import me.yuriisoft.buildnotify.mobile.network.pairing.PairingState
import me.yuriisoft.buildnotify.mobile.network.reconnection.ReconnectionStrategy
import me.yuriisoft.buildnotify.mobile.network.tls.ServerCertificateCapture
import me.yuriisoft.buildnotify.mobile.network.transport.Transport
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Control-plane implementation of [ConnectionManager].
 *
 * ### Design — Unidirectional Data Flow (MVI)
 *
 * [state] is a **derived** [StateFlow], not a manually mutated container.
 * Public methods ([connect], [disconnect]) push lightweight [Intent]s into
 * a stream; `transformLatest` maps each intent to a composable
 * `Flow<ConnectionState>` that describes the full lifecycle as a linear
 * narrative.  No mutable `Job?` fields, no `CoroutineExceptionHandler`,
 * no scattered `_state.put()` calls.
 *
 * `transformLatest` auto-cancels the previous connection when a new intent
 * arrives, so explicit `cancelAndJoin()` bookkeeping disappears entirely.
 *
 * ### Composable pipeline
 *
 * ```
 * intent stream
 *   ├─ Connect(host)
 *   │    └─ connectionFlow(host)
 *   │         ├─ emit Connecting
 *   │         ├─ pairingGate (suspend until resolved, if needed)
 *   │         └─ transportPipeline → Connected / Reconnecting / Failed / Disconnected
 *   └─ Disconnect
 *        └─ emit Disconnected
 * ```
 *
 * Each stage is a separate [Flow] or suspend function; they compose
 * via [emitAll] — easy to read, easy to test in isolation.
 *
 * ### Separation of concerns (SRP)
 *
 * | Concern               | Owner                  |
 * |-----------------------|------------------------|
 * | Connection lifecycle  | this class             |
 * | Data plane            | [SecureSession]        |
 * | Pairing flow          | [PairingCoordinator]   |
 * | Reconnection policy   | [ReconnectionStrategy] |
 * | Transport I/O         | [Transport]            |
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionOrchestrator(
    private val transport: Transport,
    private val reconnection: ReconnectionStrategy,
    private val errorMapping: Mapper<Throwable, ConnectionErrorReason>,
    private val pairingCoordinator: PairingCoordinator?,
    private val session: SecureSession,
    private val serverCertCapture: ServerCertificateCapture,
    private val helloPayload: HelloPayload,
    private val heartbeatTimeout: Duration,
    dispatchers: AppDispatchers,
) : ConnectionManager {

    private sealed interface Intent {
        data class Connect(val host: DiscoveredHost) : Intent
        data object Disconnect : Intent
    }

    private val intents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

    private val scope = CoroutineScope(dispatchers.io + SupervisorJob())

    override val state: StateFlow<ConnectionState> = intents
        .transformLatest { intent ->
            when (intent) {
                is Intent.Connect -> {
                    val host = intent.host
                    try {
                        emitAll(connectionFlow(host))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emit(ConnectionState.Failed(host, errorMapping.map(e)))
                    } finally {
                        pairingCoordinator?.reset()
                        session.deactivate()
                        transport.releaseClient(host.fingerprint.takeIf { host.isSecure })
                    }
                }

                is Intent.Disconnect -> emit(ConnectionState.Disconnected)
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, ConnectionState.Idle)

    override suspend fun connect(host: DiscoveredHost) {
        intents.emit(Intent.Connect(host))
    }

    override suspend fun disconnect() {
        intents.emit(Intent.Disconnect)
    }

    /**
     * Full connection lifecycle for [host] expressed as a linear flow:
     *
     *   1. Emit [Connecting][ConnectionState.Connecting]
     *   2. Pass through [pairingGate] (may suspend, may emit intermediate states)
     *   3. Open [transportPipeline] (emits [Connected], [Reconnecting], terminal)
     */
    private fun connectionFlow(host: DiscoveredHost): Flow<ConnectionState> = flow {
        emit(ConnectionState.Connecting(host))
        if (!pairingGate(host)) return@flow
        emitAll(transportPipeline(host))
    }

    // ── Pairing gate ─────────────────────────────────────────────────────────

    /**
     * Suspends until PIN-based pairing is resolved (if required).
     *
     * Returns `true` when the connection may proceed (pairing not needed or
     * user confirmed). Returns `false` after emitting an appropriate terminal
     * state when pairing cannot proceed.
     *
     * ### Probe-first protocol
     *
     * Before showing the PIN on the mobile side, the gate fires a **probe
     * TLS connection** to the server. The handshake is expected to fail
     * (server rejects the unknown client certificate), but it has two
     * essential side-effects:
     *
     *   1. [ServerCertificateCapture] records the **real** server certificate
     *      fingerprint (which may differ from the mDNS TXT `fp` record).
     *   2. The IDE's `ClientToFuTrustManager.onFirstSeen` callback fires,
     *      causing the IDE to open its pairing PIN dialog **before** the
     *      mobile displays its own PIN.
     *
     * This guarantees that both devices show the PIN at roughly the same
     * time, so the user can compare them side-by-side.
     *
     * This is a **suspend extension** on [FlowCollector] so it can emit
     * intermediate [ConnectionState]s while the gate is blocking.
     */
    private suspend fun FlowCollector<ConnectionState>.pairingGate(
        host: DiscoveredHost,
    ): Boolean {
        val coordinator = pairingCoordinator ?: return true
        if (!coordinator.isPairingRequired(host)) return true

        serverCertCapture.clearCapturedFingerprint()
        probeTlsHandshake(host)

        val serverFp = serverCertCapture.capturedServerFingerprint ?: host.fingerprint
        serverCertCapture.clearCapturedFingerprint()

        if (serverFp == null) {
            emit(
                ConnectionState.Failed(
                    host,
                    ConnectionErrorReason.HandshakeFailed(
                        "No server fingerprint available for pairing",
                    ),
                ),
            )
            return false
        }

        coordinator.startPairing(host, serverFp)

        val pin = (coordinator.state.value as? PairingState.AwaitingConfirmation)?.pin.orEmpty()
        emit(ConnectionState.PairingRequired(host, pin))

        val confirmed = withTimeoutOrNull(PAIRING_TIMEOUT) {
            coordinator.state
                .drop(1)
                .first { it is PairingState.Confirmed || it is PairingState.Rejected }
                .let { it is PairingState.Confirmed }
        }

        coordinator.reset()

        if (confirmed == null) {
            emit(
                ConnectionState.Failed(
                    host,
                    ConnectionErrorReason.PairingTimeout(
                        "Pairing was not completed within the time limit",
                    ),
                ),
            )
            return false
        }

        if (!confirmed) {
            emit(
                ConnectionState.Failed(
                    host,
                    ConnectionErrorReason.PairingRejected("Pairing was rejected by user"),
                ),
            )
            return false
        }

        emit(ConnectionState.Connecting(host))
        return true
    }

    /**
     * Initiates and immediately discards a TLS connection to [host].
     *
     * The handshake is expected to fail because the server has not yet
     * trusted this client's certificate. The side-effects are what matter:
     *
     *   - [ServerCertificateCapture] records the real server fingerprint.
     *   - The IDE's `ClientToFuTrustManager.onFirstSeen` fires, opening
     *     the IDE's pairing PIN dialog.
     *
     * Any exception (including the expected `CertificateException`) is
     * swallowed. [CancellationException] is rethrown to honour structured
     * concurrency. A [PROBE_TIMEOUT] caps the attempt so a slow or
     * unresponsive server cannot block the flow indefinitely.
     */
    private suspend fun probeTlsHandshake(host: DiscoveredHost) {
        val probeOutgoing = Channel<WsEnvelope>(Channel.RENDEZVOUS)
        probeOutgoing.close()
        try {
            withTimeoutOrNull(PROBE_TIMEOUT) {
                transport.open(
                    host.host, host.port, host.isSecure, host.fingerprint, probeOutgoing,
                ).first()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Expected — the server rejects the unrecognised client certificate
        }
    }

    // ── Transport pipeline ───────────────────────────────────────────────────

    /**
     * Opens the transport, wires payloads into [SecureSession], and maps
     * transport lifecycle events to [ConnectionState] emissions.
     *
     * `withIndex()` replaces a mutable "first payload" flag — the index
     * resets automatically on each `retryWhen` restart because the entire
     * upstream chain is re-collected from scratch.
     */
    @OptIn(FlowPreview::class)
    private fun transportPipeline(host: DiscoveredHost): Flow<ConnectionState> =
        channelFlow {
            val outgoing = session.activate()

            try {
                transport.open(host.host, host.port, host.isSecure, host.fingerprint, outgoing)
                    .withIndex()
                    .onEach { (index, payload) ->
                        if (index == 0) {
                            send(ConnectionState.Connected(host))
                            session.send(WsEnvelope(payload = helloPayload))
                        }
                        session.dispatch(payload)
                    }
                    .timeout(heartbeatTimeout)
                    .retryWhen { cause, attempt ->
                        this@channelFlow.send(
                            ConnectionState.Reconnecting(host, attempt + 1),
                        )
                        reconnection.shouldRetry(cause, attempt)
                    }
                    .collect()

                send(ConnectionState.Disconnected)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val reason = errorMapping.map(e)
                if (reason is ConnectionErrorReason.ClientRejected) {
                    pairingCoordinator?.unpinServer(host)
                }
                send(ConnectionState.Failed(host, reason))
            }
        }

    private companion object {
        val PROBE_TIMEOUT: Duration = 10.seconds
        val PAIRING_TIMEOUT: Duration = 120.seconds
    }
}
