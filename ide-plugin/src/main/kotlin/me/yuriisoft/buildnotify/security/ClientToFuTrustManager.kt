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
 * | Rejected       | Throws [CertificateException] with "Client explicitly rejected". |
 * | Pending        | Throws [CertificateException] with "Client approval pending".|
 * | First-seen     | Marks pending, fires [onFirstSeen], throws "pending".       |
 *
 * ## Threading
 *
 * [checkClientTrusted] is called on the java-websocket I/O thread pool.
 * [onFirstSeen] **must not block** — it is required to schedule any UI work
 * via `ApplicationManager.getApplication().invokeLater {}` and return immediately.
 * The thrown [CertificateException] propagates up through java-websocket and
 * causes the TLS handshake to fail. The mobile client's `ExponentialBackoff`
 * `retryWhen` will retry. On the next attempt, if the user has approved,
 * [TrustedClients.isTrusted] returns `true` and the handshake succeeds.
 *
 * ## SRP note
 *
 * This class knows only about JSSE semantics and the [TrustedClients] store.
 * Dialog creation is the caller's responsibility, expressed through [onFirstSeen].
 *
 * @param store          The persistent approval store.
 * @param onFirstSeen    Non-blocking callback fired when an unknown certificate is seen for the first time. Receives the SHA-256
 * fingerprint formatted as `AB:CD:EF:...`.
 */
class ClientToFuTrustManager(
    private val store: PersistentTrustedClients,
    private val onFirstSeen: (fingerprint: String) -> Unit,
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

        val fingerprint = chain[0].sha256Fingerprint()

        when {
            store.isTrusted(fingerprint) -> return

            store.isRejected(fingerprint) ->
                throw CertificateException("Client explicitly rejected")

            store.isPending(fingerprint) ->
                throw CertificateException("Client approval pending")

            else -> {
                store.markPending(fingerprint)
                onFirstSeen(fingerprint)   // non-blocking — schedules dialog on EDT
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
}