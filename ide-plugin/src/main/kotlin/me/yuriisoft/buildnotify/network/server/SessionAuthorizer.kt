package me.yuriisoft.buildnotify.network.server

import me.yuriisoft.buildnotify.security.sha256Fingerprint
import org.java_websocket.WebSocket
import java.security.cert.X509Certificate

/**
 * Decides whether a connected client may receive build data and send commands.
 *
 * TLS (`needClientAuth`) already refuses unknown clients at the handshake; this is the
 * second line of defence that keeps working *after* the handshake — a device revoked
 * in Settings loses access immediately instead of at its next reconnect.
 */
fun interface SessionAuthorizer {
    fun isAuthorized(clientFingerprint: String?): Boolean
}

/**
 * Fingerprint of the certificate the peer presented during the TLS handshake,
 * or `null` for a plain connection or a peer without a certificate.
 */
internal fun WebSocket.peerCertificateFingerprint(): String? = runCatching {
    if (!hasSSLSupport()) return@runCatching null
    (sslSession.peerCertificates.firstOrNull() as? X509Certificate)?.sha256Fingerprint()
}.getOrNull()
