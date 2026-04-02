package me.yuriisoft.buildnotify.mobile.network.tls

/**
 * Provides access to the server certificate fingerprint observed during the most
 * recent TLS handshake.
 *
 * Platform TLS implementations (Android's `BuildNotifyTrustManager`, iOS's Darwin
 * challenge handler) capture the server certificate's SHA-256 fingerprint during
 * `checkServerTrusted` / `ServerTrust` challenge evaluation — **even when the
 * handshake ultimately fails** (e.g. the server rejects the client certificate
 * during pairing).
 *
 * The captured fingerprint is used by [PairingCoordinator] to derive the 6-digit
 * PIN from the **actual** TLS certificate rather than the mDNS-advertised `fp`
 * TXT record, which could theoretically be spoofed.
 *
 * ### Thread safety
 *
 * Implementations must be safe to call from any thread. The fingerprint is written
 * on the TLS handshake thread and read on a coroutine dispatcher.
 */
interface ServerCertificateCapture {

    /**
     * The SHA-256 fingerprint of the server certificate observed during the most
     * recent TLS handshake, in colon-separated uppercase hex format
     * (e.g. `"AB:CD:EF:12:34:..."`).
     *
     * `null` if no handshake has occurred yet or if [clearCapturedFingerprint]
     * was called after the last handshake.
     */
    val capturedServerFingerprint: String?

    /**
     * Clears the captured fingerprint. Call after the fingerprint has been
     * consumed (e.g. after PIN derivation) to avoid stale values on subsequent
     * reads.
     */
    fun clearCapturedFingerprint()
}
