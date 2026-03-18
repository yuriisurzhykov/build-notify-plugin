package me.yuriisoft.buildnotify.server

import org.java_websocket.WebSocket
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Immutable value object: identity + transportation.
 * Does not store a state, it only delegates `send` to socket.
 * */
data class WebSocketSession @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val socket: WebSocket
) {
    val isOpen: Boolean get() = socket.isOpen

    fun send(message: String) {
        if (isOpen) socket.send(message)
    }
}