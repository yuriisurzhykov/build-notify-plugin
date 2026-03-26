package me.yuriisoft.buildnotify.mobile.network.tls

/**
 * Persistent store of TOFU-pinned server certificate fingerprints.
 *
 * Keys are server [instanceId]s (from the handshake payload); values are
 * SHA-256 certificate fingerprints in colon-separated hex format
 * (e.g. `"AB:CD:EF:12:..."`).
 *
 * Implementations must persist data across app restarts.
 */
interface TrustedServers {

    fun fingerprint(instanceId: String): String?

    fun pin(instanceId: String, fingerprint: String)

    fun unpin(instanceId: String)

    fun isPinned(instanceId: String): Boolean = fingerprint(instanceId) != null

    fun matches(instanceId: String, fingerprint: String): Boolean =
        this.fingerprint(instanceId).equals(fingerprint, ignoreCase = true)
}
