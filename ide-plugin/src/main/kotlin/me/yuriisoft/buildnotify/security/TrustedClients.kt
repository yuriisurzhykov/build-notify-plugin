package me.yuriisoft.buildnotify.security

/**
 * Server-side mirror of the mobile's `TrustedServers`.
 *
 * Tracks which client certificates (identified by SHA-256 fingerprint) are
 * allowed to complete a mutual-TLS handshake with this plugin.
 *
 * State machine per fingerprint:
 *   unknown  →  [markPending]  →  pending
 *   pending  →  [trust]        →  trusted
 *   pending  →  [reject]       →  rejected
 *   trusted  →  [revoke]       →  unknown  (removed from store)
 *
 * All methods must be thread-safe — [checkClientTrusted] is called on the
 * java-websocket I/O thread pool, concurrently with IDE EDT writes.
 */
interface TrustedClients {

    /** Returns `true` iff the fingerprint has been explicitly trusted by the user. */
    fun isTrusted(clientFingerprint: String): Boolean

    /**
     * Returns `true` iff the fingerprint has been seen but not yet decided upon.
     * A pending client causes the handshake to fail with a retriable exception
     * until the user approves or rejects via [ClientApprovalDialog].
     */
    fun isPending(clientFingerprint: String): Boolean

    /**
     * Marks the fingerprint as pending (first-seen).
     * No-op if already trusted or already pending.
     */
    fun markPending(clientFingerprint: String)

    /** Moves a fingerprint from pending → trusted. Idempotent. */
    fun trust(clientFingerprint: String)

    /**
     * Moves a fingerprint from pending → rejected.
     * Subsequent connections from this client will receive
     * `CertificateException("Client explicitly rejected")`.
     */
    fun reject(clientFingerprint: String)

    /**
     * Removes a previously trusted fingerprint entirely.
     * The next connection attempt from that client will re-trigger the
     * approval dialog (first-seen flow).
     */
    fun revoke(clientFingerprint: String)

    /** Snapshot of all trusted fingerprints. Suitable for settings UI display. */
    fun trustedFingerprints(): Set<String>
}