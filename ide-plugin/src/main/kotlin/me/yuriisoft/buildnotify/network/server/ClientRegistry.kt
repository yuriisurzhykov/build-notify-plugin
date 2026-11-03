package me.yuriisoft.buildnotify.network.server

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import me.yuriisoft.buildnotify.security.PersistentTrustedClients
import org.java_websocket.framing.CloseFrame
import java.util.concurrent.ConcurrentHashMap

/**
 * Connected clients. Every outgoing message passes [SessionAuthorizer]: a session
 * whose certificate is no longer trusted receives nothing and is closed.
 */
@Service(Service.Level.APP)
class ClientRegistry {

    // Light services must keep a single no-arg constructor; the trust decision itself
    // lives in TrustedClientAuthorizer, which is unit-tested on its own.
    private val authorizer: SessionAuthorizer =
        TrustedClientAuthorizer { service<PersistentTrustedClients>() }

    private val clients = ConcurrentHashMap<String, WebSocketSession>()

    fun register(client: WebSocketSession) {
        clients[client.id] = client
    }

    fun unregister(clientId: String?) {
        if (clientId == null) return
        clients.remove(clientId)
    }

    fun isAuthorized(session: WebSocketSession): Boolean =
        authorizer.isAuthorized(session.clientFingerprint)

    fun broadcast(message: String) {
        removeClosedSessions()
        clients.values.forEach { session -> deliver(session, message) }
    }

    fun sendTo(sessionId: String, message: String) {
        clients[sessionId]?.let { deliver(it, message) }
    }

    fun findSession(sessionId: String): WebSocketSession? = clients[sessionId]

    /**
     * Closes every session that is no longer authorized, e.g. right after the user
     * revokes a device in Settings. Returns how many sessions were closed.
     */
    fun disconnectUnauthorized(): Int {
        val revoked = clients.values.filter { it.isOpen && !isAuthorized(it) }
        revoked.forEach { it.close(CloseFrame.POLICY_VALIDATION, REASON_NOT_PAIRED) }
        return revoked.size
    }

    private fun deliver(session: WebSocketSession, message: String) {
        if (!session.isOpen) return
        if (isAuthorized(session)) {
            session.send(message)
        } else {
            session.close(CloseFrame.POLICY_VALIDATION, REASON_NOT_PAIRED)
        }
    }

    private fun removeClosedSessions() {
        clients.entries.removeIf { !it.value.isOpen }
    }

    val connectedCount: Int
        get() = clients.values.count(WebSocketSession::isOpen)

    fun hasNoOpenClients(): Boolean =
        clients.values.none(WebSocketSession::isOpen)

    internal companion object {
        const val REASON_NOT_PAIRED = "Client is not paired with this IDE"
    }
}
