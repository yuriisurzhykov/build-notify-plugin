package me.yuriisoft.buildnotify.network.server

import org.java_websocket.WebSocket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Server-side representation of a single connected client.
 *
 * Wraps the raw [WebSocket] with a stable [id] for registry lookups and
 * carries mutable session metadata ([deviceName]) that arrives after the
 * initial TLS handshake — specifically from the `sys.hello` message the
 * client sends once the connection is established.
 *
 * Not a `data class` intentionally: [deviceName] is set asynchronously
 * after construction, and equality/hashCode based on a mutable field
 * would break [ClientRegistry]'s ConcurrentHashMap contract.
 */
class WebSocketSession @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val socket: WebSocket,
) {
    val isOpen: Boolean get() = socket.isOpen

    /**
     * Human-readable name of the connected device, populated from
     * [me.yuriisoft.buildnotify.serialization.HelloPayload.deviceName]
     * when the server receives `sys.hello`.
     *
     * Empty until the client completes the hello exchange.
     */
    @Volatile
    var deviceName: String = ""

    fun send(message: String) {
        if (isOpen) socket.send(message)
    }
}