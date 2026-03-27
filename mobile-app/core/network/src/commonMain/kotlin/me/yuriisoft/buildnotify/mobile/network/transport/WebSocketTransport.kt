package me.yuriisoft.buildnotify.mobile.network.transport

import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.network.client.HttpClientProvider

/**
 * Raw bidirectional WebSocket channel — the only class that touches Ktor
 * WebSocket APIs directly (SRP: I/O only).
 *
 * Returns a cold [Flow] of decoded payloads. Takes a [ReceiveChannel] for
 * outgoing messages — the actor pattern replaces the old `sendMutex`.
 *
 * Each [open] call creates a fresh [HttpClient] via [clientProvider] with
 * the supplied [fingerprint] so TLS certificate pinning is configured
 * per-connection (TOFU). The client is closed in the `finally` block.
 *
 * When the flow is cancelled or errors, `finally` cleans up the sender
 * coroutine, the WebSocket session, and the client. When `retryWhen`
 * (applied downstream) restarts the flow, a fresh session is created.
 */
class WebSocketTransport(
    private val clientProvider: HttpClientProvider,
    private val codec: PayloadCodec,
) : Transport {

    override fun open(
        host: String,
        port: Int,
        secure: Boolean,
        fingerprint: String?,
        outgoing: ReceiveChannel<WsEnvelope>,
    ): Flow<WsPayload> = channelFlow {
        val client = clientProvider.provide(fingerprint.takeIf { secure })

        val ws = client.webSocketSession {
            url {
                protocol = if (secure) URLProtocol.WSS else URLProtocol.WS
                this.host = host
                this.port = port
                pathSegments = listOf("ws")
            }
        }

        val sender = launch {
            for (envelope in outgoing) {
                ws.send(Frame.Text(codec.encode(envelope)))
            }
        }

        try {
            ws.incoming
                .receiveAsFlow()
                .mapNotNull { (it as? Frame.Text)?.readText() }
                .map { codec.decode(it).payload }
                .collect { send(it) }
        } finally {
            sender.cancel()
            ws.close()
            // client is intentionally not closed here — it is reused across
            // retryWhen cycles. ManagedConnection.disconnect() calls
            // releaseClient() when the connection is explicitly torn down.
        }
    }

    override fun releaseClient(fingerprint: String?) {
        clientProvider.release(fingerprint)
    }
}