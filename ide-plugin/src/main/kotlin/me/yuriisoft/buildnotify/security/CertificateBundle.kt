package me.yuriisoft.buildnotify.security

import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext

/**
 * Immutable value object produced by [SslContextProvider] implementations.
 *
 * [keyManagers] are exposed separately so that consumers (e.g. mTLS setup)
 * can build a new [SSLContext] that combines the server's key material with
 * an additional [javax.net.ssl.TrustManager], without mutating [sslContext]
 * in place or reaching into its internal state.
 *
 * [SSLContext] does not expose a `getKeyManagers()` API — once initialised,
 * there is no way to retrieve the [KeyManager]s from the outside. Storing
 * them here at construction time is the only clean solution.
 */
data class CertificateBundle(
    val sslContext: SSLContext,
    val fingerprint: String,
    val keyManagers: Array<KeyManager>,
) {
    // Custom equals/hashCode because Array doesn't implement structural equality.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CertificateBundle) return false
        return sslContext == other.sslContext &&
                fingerprint == other.fingerprint &&
                keyManagers.contentEquals(other.keyManagers)
    }

    override fun hashCode(): Int {
        var result = sslContext.hashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + keyManagers.contentHashCode()
        return result
    }
}