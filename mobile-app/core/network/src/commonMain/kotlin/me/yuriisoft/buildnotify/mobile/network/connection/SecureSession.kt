package me.yuriisoft.buildnotify.mobile.network.connection

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload

/**
 * Data-plane implementation of [ActiveSession].
 *
 * Owns the incoming payload stream and the outgoing message channel but
 * has **no knowledge** of connection lifecycle, reconnection, or pairing
 * (SRP). The [ConnectionOrchestrator] calls [activate] to open a fresh
 * outgoing channel and [deactivate] to tear it down.
 *
 * ### Lifecycle contract
 *
 * ```
 * activate()    →  creates + returns a Channel, session is writable
 * deactivate()  →  closes the channel, send() will throw until next activate()
 * activate()    →  implicitly deactivates the previous channel first
 * ```
 */
class SecureSession : ActiveSession {

    private val _incoming = MutableSharedFlow<WsPayload>(extraBufferCapacity = INCOMING_BUFFER)

    override val incoming: SharedFlow<WsPayload> = _incoming.asSharedFlow()

    @Volatile
    private var currentOutgoing: Channel<WsEnvelope>? = null

    override suspend fun send(envelope: WsEnvelope) {
        val ch = currentOutgoing ?: error("Not connected — no active session")
        ch.send(envelope)
    }

    /**
     * Creates a fresh outgoing [Channel] for the new transport pipeline
     * and returns it so the caller can pass it to [Transport.open].
     * Implicitly [deactivate]s the previous channel if one exists.
     */
    internal fun activate(): Channel<WsEnvelope> {
        deactivate()
        return Channel<WsEnvelope>(Channel.BUFFERED).also { currentOutgoing = it }
    }

    /**
     * Closes the current outgoing channel, making [send] throw until the
     * next [activate]. Idempotent — safe to call when already deactivated.
     */
    internal fun deactivate() {
        currentOutgoing?.close()
        currentOutgoing = null
    }

    /**
     * Forwards a decoded payload from the transport into the [incoming] stream.
     * Called by [ConnectionOrchestrator] for every payload received from the wire.
     */
    internal suspend fun dispatch(payload: WsPayload) {
        _incoming.emit(payload)
    }

    private companion object {
        const val INCOMING_BUFFER = 64
    }
}
