package me.yuriisoft.buildnotify.network.server

import org.java_websocket.WebSocket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * One connected client.
 *
 * @property clientFingerprint fingerprint of the certificate presented in the TLS
 *   handshake — the client's identity, checked by [SessionAuthorizer] before any
 *   data is delivered.
 */
class WebSocketSession @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val socket: WebSocket,
    val clientFingerprint: String?,
) {
    val isOpen: Boolean get() = socket.isOpen

    @Volatile
    var deviceName: String = ""

    fun send(message: String) {
        if (isOpen) socket.send(message)
    }

    fun close(code: Int, reason: String) {
        if (isOpen) socket.close(code, reason)
    }
}
