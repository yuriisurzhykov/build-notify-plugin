package me.yuriisoft.buildnotify.server

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.serialization.MessageSerializer
import me.yuriisoft.buildnotify.serialization.WsMessage
import me.yuriisoft.buildnotify.settings.PluginSettings
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Application-level service: lives in the IDE forever.
 *
 * IntelliJ Platform 2023.2+ resolves the @Service constructor automatically.
 * ClientRegistry and PluginSettings are also @Service(APP), injected by the platform.
 *
 * HeartbeatScheduler is not a service, so we create it here.
 * But we pass in predefined dependencies, which means we don't violate DIP.
 */
@Service(Service.Level.APP)
class BuildWebSocketServer : AutoCloseable {

    private val logger = thisLogger()
    private val isRunning = AtomicBoolean(false)
    private var server: InternalServer? = null
    private var heartbeatScheduler: HeartbeatScheduler? = null

    fun start() {
        if (isRunning.getAndSet(true)) return

        val settings: PluginSettings = service()
        val registry: ClientRegistry = service()

        val port = settings.state.port
        server = InternalServer(port, registry).apply {
            isReuseAddr = true
            start()
        }
        logger.info("WebSocket server started on port $port.")

        heartbeatScheduler = HeartbeatScheduler(registry, settings).also { it.start() }
    }

    fun broadcast(message: WsMessage) {
        val encodedMessage = MessageSerializer.encode(message)
        val registry: ClientRegistry = service()
        registry.broadcast(encodedMessage)
    }

    fun isActive(): Boolean = isRunning.get()

    override fun close() {
        heartbeatScheduler?.stop()
        server?.stop(1000)
        server = null
        isRunning.set(false)
        logger.info("WebSocket server stopped.")
    }

    private inner class InternalServer(
        port: Int,
        private val registry: ClientRegistry,
    ) : WebSocketServer(InetSocketAddress(port)) {
        override fun onOpen(
            conn: WebSocket,
            handshake: ClientHandshake
        ) {
            val session = WebSocketSession(socket = conn)
            conn.setAttachment(session.id)
            registry.register(session)
            logger.info(
                "Client connected: ${conn.remoteSocketAddress}, " +
                        "total connected=${registry.connectedCount}"
            )
        }

        override fun onClose(
            conn: WebSocket,
            code: Int,
            reason: String?,
            remote: Boolean
        ) {
            val sessionId = conn.getAttachment<String>()
            registry.unregister(sessionId)
            logger.info(
                "Client disconnected: ${conn.remoteSocketAddress}, " +
                        "reason: $reason, " +
                        "remote: $remote, " +
                        "remaining=${registry.connectedCount}"
            )
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            runCatching { MessageSerializer.decode(message.orEmpty()) }
                .onSuccess { handleIncomingMessage(it) }
                .onFailure { e -> logger.warn("Failed to parse message: '$message'", e) }
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            logger.error("WebSocket connection error on ${conn?.remoteSocketAddress}", ex)
        }

        override fun onStart() {
            logger.info("InternalServer onStart()")
        }

        private fun handleIncomingMessage(message: WsMessage) {
            logger.debug("InternalServer handleIncomingMessage: $message)")
        }
    }
}