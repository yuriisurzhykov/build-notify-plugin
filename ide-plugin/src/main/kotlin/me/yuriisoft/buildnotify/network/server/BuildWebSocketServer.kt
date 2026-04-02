package me.yuriisoft.buildnotify.network.server

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.BuildNotifyBundle
import me.yuriisoft.buildnotify.build.BuildSnapshotProvider
import me.yuriisoft.buildnotify.network.discovery.InstanceIdentity
import me.yuriisoft.buildnotify.network.server.ui.PairingPinDialog
import me.yuriisoft.buildnotify.notification.PluginNotifier
import me.yuriisoft.buildnotify.security.CertificateManager
import me.yuriisoft.buildnotify.security.ClientToFuTrustManager
import me.yuriisoft.buildnotify.security.PersistentTrustedClients
import me.yuriisoft.buildnotify.serialization.*
import me.yuriisoft.buildnotify.settings.PluginSettingsState
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages the lifecycle of the WebSocket server that pushes build events to
 * connected mobile clients.
 *
 * ### Phase 3 change — `instanceId` delegation
 * `instanceId` is no longer generated locally. It is resolved from the
 * [InstanceIdentity] application service, which is also referenced by
 * [MdnsAdvertiser] to include the same `id` in the mDNS TXT record.
 *
 * This removes the inconsistency where the WebSocket handshake payload
 * advertised one UUID and the mDNS record advertised nothing — now both
 * layers surface the same identity token.
 *
 * Everything else (server lifecycle, broadcast, SSL setup, heartbeat) is
 * unchanged; this is a minimal, surgical modification (YAGNI, OCP).
 */
@Service(Service.Level.APP)
class BuildWebSocketServer : Disposable {

    private val logger = thisLogger()
    private val running = AtomicBoolean(false)

    private val instanceId: String = service<InstanceIdentity>().id

    private var server: InternalWebSocketServer? = null
    private var heartbeatScheduler: HeartbeatScheduler? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return

        runCatching {
            val settings = service<PluginSettingsState>().snapshot()
            val registry = service<ClientRegistry>()

            server = InternalWebSocketServer(
                port = settings.port,
                registry = registry,
            ).apply {
                isReuseAddr = true
                connectionLostTimeout = settings.connectionLostTimeoutSec
                start()
            }

            heartbeatScheduler = HeartbeatScheduler(
                registry = registry,
                settingsProvider = { service<PluginSettingsState>().snapshot() },
            ).also { it.start() }

            logger.info("WebSocket server started on port ${settings.port}")
            service<PluginNotifier>().info(
                BuildNotifyBundle.message("notification.server.started.title"),
                BuildNotifyBundle.message("notification.server.started.content", settings.port),
            )
        }.onFailure { throwable ->
            running.set(false)
            heartbeatScheduler?.stop()
            heartbeatScheduler = null
            server = null
            logger.error("Failed to start WebSocket server", throwable)
            service<PluginNotifier>().error(
                BuildNotifyBundle.message("notification.server.start.failed.title"),
                BuildNotifyBundle.message(
                    "notification.server.start.failed.content",
                    throwable.message.orEmpty(),
                ),
            )
        }
    }

    fun isActive(): Boolean = running.get()

    /**
     * Broadcast a payload to all connected clients.
     * The server wraps it in a fresh [WsEnvelope] automatically.
     */
    fun broadcast(payload: WsPayload) {
        if (!isActive()) return
        val encoded = MessageSerializer.encode(WsEnvelope(payload = payload))
        service<ClientRegistry>().broadcast(encoded)
    }

    /**
     * Send a typed response to the specific client that issued [command].
     * Sets `correlationId = command.id` so the client can match request ↔ response.
     */
    fun replyTo(command: WsEnvelope, sessionId: String, payload: WsPayload) {
        if (!isActive()) return
        val encoded = MessageSerializer.encode(WsEnvelope(correlationId = command.id, payload = payload))
        service<ClientRegistry>().sendTo(sessionId, encoded)
    }

    fun stop() {
        heartbeatScheduler?.stop()
        heartbeatScheduler = null

        runCatching { server?.stop(1_000) }
            .onFailure { throwable -> logger.warn("Failed to stop WebSocket server cleanly", throwable) }

        server = null
        running.set(false)

        logger.info("WebSocket server stopped")
    }

    override fun dispose() = stop()

    private inner class InternalWebSocketServer(
        port: Int,
        private val registry: ClientRegistry,
    ) : WebSocketServer(InetSocketAddress(port)) {

        init {
            setupSSL(port)
        }

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            val session = WebSocketSession(socket = conn)
            conn.setAttachment(session.id)
            registry.register(session)

            val greeting = WsEnvelope(
                payload = HandshakePayload(
                    instanceId = instanceId,
                    capabilities = setOf(
                        Capability.BUILD_MONITOR,
                        Capability.BUILD_CONTROL,
                    ),
                    certFingerprint = service<CertificateManager>().fingerprint(),
                    deviceName = ApplicationNamesInfo.getInstance().fullProductName,
                ),
            )
            session.send(MessageSerializer.encode(greeting))

            logger.info("Client connected: ${conn.remoteSocketAddress}, total=${registry.connectedCount}")
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
            registry.unregister(conn.getAttachment())
            logger.info(
                "Client disconnected: ${conn.remoteSocketAddress}, reason=$reason, remote=$remote, remaining=${registry.connectedCount}",
            )
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            val raw = message.orEmpty()
            val sessionId: String? = conn?.getAttachment()
            runCatching {
                val envelope = MessageSerializer.decode(raw)
                handleClientMessage(envelope, sessionId)
            }.onFailure { throwable ->
                logger.warn("Failed to decode incoming message: '$raw'", throwable)
            }
        }

        /**
         * Dispatch point for all client-initiated messages.
         * Add new command handlers here as a new `is XxxCommand` branch — nothing else changes.
         */
        private fun handleClientMessage(envelope: WsEnvelope, sessionId: String?) {
            if (sessionId == null) {
                logger.warn("Received message from unknown session, ignoring")
                return
            }

            when (val payload = envelope.payload) {
                is HelloPayload -> handleHello(payload, sessionId)

                is CancelBuildCommand -> {
                    // TODO: delegate to BuildMonitorService.cancelBuild(payload.buildId)
                    replyTo(envelope, sessionId, CommandResultPayload(CommandStatus.ACCEPTED))
                    logger.debug("CancelBuild requested for buildId=${payload.buildId}")
                }

                is RunBuildCommand -> {
                    // TODO: delegate to GradleRunner / ExternalSystemManager
                    replyTo(envelope, sessionId, CommandResultPayload(CommandStatus.ACCEPTED))
                    logger.debug("RunBuild requested: project=${payload.projectName}, tasks=${payload.tasks}")
                }

                else -> logger.warn(
                    "Received unexpected payload type from client: ${payload::class.simpleName}",
                )
            }
        }

        private fun handleHello(payload: HelloPayload, sessionId: String) {
            logger.info(
                "Client hello: device=${payload.deviceName}, platform=${payload.platform}, version=${payload.appVersion}",
            )

            registry.findSession(sessionId)?.deviceName = payload.deviceName

            val snapshot = service<BuildSnapshotProvider>().snapshot()
            val encoded = MessageSerializer.encode(WsEnvelope(payload = snapshot))
            registry.sendTo(sessionId, encoded)
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            logger.warn("WebSocket error on ${conn?.remoteSocketAddress}", ex)
        }

        override fun onStart() {
            logger.info("Internal WebSocket server is ready")
        }

        private fun setupSSL(port: Int) {
            val bundle = CertificateManager.getInstance().bundle()

            if (bundle == null) {
                logger.warn("TLS unavailable; running plain WS on port $port")
                service<PluginNotifier>().warning(
                    BuildNotifyBundle.message("notification.ssl.failed.title"),
                    BuildNotifyBundle.message(
                        "notification.ssl.failed.content",
                        "All SSL providers failed. Check Event Log."
                    ),
                )
                return
            }

            val clientTrustManager = ClientToFuTrustManager(
                store = service<PersistentTrustedClients>(),
                serverFingerprint = bundle.fingerprint,
                onFirstSeen = { serverFp, clientFp, deviceName ->
                    showPairingDialogOnEdt(serverFp, clientFp, deviceName)
                },
            )

            setWebSocketFactory(
                MtlsWebSocketServerFactory(
                    serverKeyManagers = bundle.keyManagers,
                    clientTrustManager = clientTrustManager,
                )
            )

            logger.info("SSL (WSS) with mutual-TLS (wantClientAuth) configured on port $port")
            service<PluginNotifier>().info(
                BuildNotifyBundle.message("notification.ssl.enabled.title"),
                BuildNotifyBundle.message("notification.ssl.enabled.content", port),
            )
        }

        /**
         * Posts [PairingPinDialog] creation to the EDT.
         *
         * Called from the java-websocket I/O thread inside
         * [ClientToFuTrustManager.checkClientTrusted]. Must not block —
         * `invokeLater` returns immediately, allowing the I/O thread to
         * propagate the [java.security.cert.CertificateException] that
         * rejects this handshake attempt. The mobile client's reconnection
         * strategy will retry; on the next attempt, if the user confirmed
         * the PIN, [me.yuriisoft.buildnotify.security.TrustedClients.isTrusted]
         * returns `true` and the handshake succeeds.
         */
        private fun showPairingDialogOnEdt(
            serverFingerprint: String,
            clientFingerprint: String,
            deviceName: String,
        ) {
            ApplicationManager.getApplication().invokeLater {
                logger.info("Showing pairing dialog for device '$deviceName'")
                PairingPinDialog(
                    clientFingerprint = clientFingerprint,
                    serverFingerprint = serverFingerprint,
                    deviceName = deviceName,
                ).show()
            }
        }
    }
}