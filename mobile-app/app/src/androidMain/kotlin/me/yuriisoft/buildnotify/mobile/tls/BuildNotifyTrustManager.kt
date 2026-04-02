package me.yuriisoft.buildnotify.mobile.tls

import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Custom [X509TrustManager] that accepts a server certificate only when its
 * SHA-256 fingerprint matches the TOFU-pinned [expectedFingerprint].
 *
 * Fingerprints are compared case-insensitively in colon-separated hex format
 * (e.g. `"AB:CD:EF:12:34:..."`).
 *
 * ### Server certificate capture (pairing support)
 *
 * [onServerCertificateObserved] is invoked with the server certificate's SHA-256
 * fingerprint **before** the validation decision is made. This allows the pairing
 * flow to capture the actual TLS fingerprint even when the handshake ultimately
 * fails (e.g. the server rejects the client certificate). The captured fingerprint
 * is used to derive the 6-digit PIN from the real certificate rather than the
 * mDNS-advertised value.
 *
 * @param expectedFingerprint         the TOFU-pinned fingerprint to validate against.
 * @param onServerCertificateObserved callback receiving the server certificate's
 *                                    SHA-256 fingerprint (colon-separated hex).
 *                                    Invoked on the TLS handshake thread.
 */
class BuildNotifyTrustManager(
    private val expectedFingerprint: String,
    private val onServerCertificateObserved: (fingerprint: String) -> Unit = {},
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        throw CertificateException("Client certificates are not supported")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        if (chain.isEmpty()) throw CertificateException("Empty certificate chain")

        val leaf = chain[0]
        val actual = leaf.sha256Fingerprint()

        onServerCertificateObserved(actual)

        if (!actual.equals(expectedFingerprint, ignoreCase = true)) {
            throw CertificateException(
                "Certificate fingerprint mismatch. Expected: $expectedFingerprint, got: $actual"
            )
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    private fun X509Certificate.sha256Fingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(encoded)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
