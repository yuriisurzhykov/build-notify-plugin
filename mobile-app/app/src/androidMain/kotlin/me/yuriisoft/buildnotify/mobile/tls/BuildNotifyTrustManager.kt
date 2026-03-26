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
 */
class BuildNotifyTrustManager(
    private val expectedFingerprint: String,
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        throw CertificateException("Client certificates are not supported")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        if (chain.isEmpty()) throw CertificateException("Empty certificate chain")

        val leaf = chain[0]
        val actual = leaf.sha256Fingerprint()

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
