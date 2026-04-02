package me.yuriisoft.buildnotify.security

import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Server-side [X509TrustManager] that enforces a Trust-On-First-Use policy
 * for client certificates.
 *
 * ## Contract
 *
 * | Client state   | Action in [checkClientTrusted]                              |
 * |----------------|-------------------------------------------------------------|
 * | No cert sent   | Returns normally — compatibility with pre-mTLS clients.    |
 * | Trusted        | Returns normally — handshake proceeds.                      |
 * | Rejected       | Revokes rejection, marks pending, fires [onFirstSeen], throws "pending". |
 * | Pending        | Throws [CertificateException] with "Client approval pending".|
 * | First-seen     | Marks pending, fires [onFirstSeen], throws "pending".       |
 *
 * ## Threading
 *
 * [checkClientTrusted] is called on the java-websocket I/O thread pool.
 * [onFirstSeen] **must not block** — it is required to schedule any UI work
 * via `ApplicationManager.getApplication().invokeLater {}` and return immediately.
 * The thrown [CertificateException] propagates up through java-websocket and
 * causes the TLS handshake to fail. The mobile client's reconnection strategy
 * will retry. On the next attempt, if the user has approved,
 * [TrustedClients.isTrusted] returns `true` and the handshake succeeds.
 *
 * **Rejection is not permanent.** When a previously-rejected fingerprint
 * reconnects, its rejection is revoked and it transitions back to pending
 * (re-triggering [onFirstSeen]). This allows re-pairing without manual
 * intervention in Settings. Users who want a permanent ban should use the
 * "Revoke" action in the Trusted Clients settings panel.
 *
 * ## SRP note
 *
 * This class knows only about JSSE semantics and the [TrustedClients] store.
 * Dialog creation is the caller's responsibility, expressed through [onFirstSeen].
 *
 * @param store              The persistent approval store.
 * @param serverFingerprint  SHA-256 fingerprint of the server's own certificate
 *                           (`AB:CD:EF:...`). Passed through to [onFirstSeen]
 *                           so the caller can compute the pairing PIN from the
 *                           combination of both fingerprints.
 * @param onFirstSeen        Non-blocking callback fired when an unknown certificate
 *                           is seen for the first time. Receives the server
 *                           fingerprint, the client SHA-256 fingerprint
 *                           (`AB:CD:EF:...`), and the device name extracted
 *                           from the certificate's Subject CN field.
 */
class ClientToFuTrustManager(
    private val store: PersistentTrustedClients,
    private val serverFingerprint: String,
    private val onFirstSeen: (serverFingerprint: String, clientFingerprint: String, deviceName: String) -> Unit,
) : X509TrustManager {

    /**
     * Not used for client verification — this manager lives on the server side.
     * Delegates to the system trust store would be wrong here; we simply declare
     * that we do not validate server chains (the server *is* us).
     */
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

    /**
     * Core TOFU logic. See class-level contract table.
     *
     * An empty [chain] means the client chose not to present a certificate
     * (valid when `wantClientAuth = true`). We allow this to maintain backward
     * compatibility with iOS clients that don't yet implement mTLS (Phase 4).
     */
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        if (chain.isEmpty()) return  // no cert presented — non-mTLS client, allow through

        val cert = chain[0]
        val fingerprint = cert.sha256Fingerprint()

        when {
            store.isTrusted(fingerprint) -> return

            store.isRejected(fingerprint) -> {
                store.revoke(fingerprint)
                store.markPending(fingerprint)
                onFirstSeen(serverFingerprint, fingerprint, cert.commonName())
                throw CertificateException("Client approval pending")
            }

            store.isPending(fingerprint) ->
                throw CertificateException("Client approval pending")

            else -> {
                store.markPending(fingerprint)
                onFirstSeen(serverFingerprint, fingerprint, cert.commonName())
                throw CertificateException("Client approval pending")
            }
        }
    }

    /**
     * We are not a CA and do not impose issuer constraints on client certificates.
     * Returning an empty array tells the JSSE stack to accept any issuer.
     */
    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    // ── Extension ─────────────────────────────────────────────────────────────

    /**
     * Computes the SHA-256 fingerprint of [this] certificate's DER-encoded form,
     * formatted as colon-separated uppercase hex pairs: `AB:CD:EF:...`
     *
     * Matches the format produced by the mobile's `ClientCertificateManager.fingerprint()`.
     */
    private fun X509Certificate.sha256Fingerprint(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(encoded)
            .joinToString(":") { byte -> "%02X".format(byte) }

    /**
     * Extracts the Common Name (CN) from the certificate's Subject DN.
     * Falls back to `"Unknown Device"` when the CN is absent — this keeps
     * the pairing dialog usable even if the mobile client generates a cert
     * without a meaningful CN.
     */
    private fun X509Certificate.commonName(): String =
        subjectX500Principal.name
            .split(",")
            .asSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("CN=", ignoreCase = true) }
            ?.substringAfter("=")
            ?.trim()
            ?: UNKNOWN_DEVICE_NAME

    private companion object {
        const val UNKNOWN_DEVICE_NAME = "Unknown Device"
    }
}