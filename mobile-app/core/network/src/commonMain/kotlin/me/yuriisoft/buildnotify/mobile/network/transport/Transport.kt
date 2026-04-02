package me.yuriisoft.buildnotify.mobile.network.transport

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionOrchestrator

/**
 * Abstraction over the raw bidirectional WebSocket channel (DIP).
 *
 * [ConnectionOrchestrator] depends on this interface, not on [WebSocketTransport]
 * directly, enabling substitution with a fake in tests and alternative
 * transport implementations in the future.
 *
 * `fingerprint` is the TOFU-pinned (Trust on first use) SHA-256 fingerprint. When non-null,
 * the transport configures TLS certificate pinning for the connection.
 */
fun interface Transport {
    fun open(
        host: String,
        port: Int,
        secure: Boolean,
        fingerprint: String?,
        outgoing: ReceiveChannel<WsEnvelope>,
    ): Flow<WsPayload>

    /**
     * Releases any resources (e.g. cached [HttpClient]) held for [fingerprint].
     *
     * Called by [ConnectionOrchestrator] on explicit disconnect — not during
     * retryWhen cycles, so the transport can reuse the same client across
     * reconnect attempts to the same host.
     *
     * No-op by default.
     */
    fun releaseClient(fingerprint: String?) {}
}
