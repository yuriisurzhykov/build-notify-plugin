package me.yuriisoft.buildnotify.security

/** Test double for [TrustedClients] with the same state transitions as the persistent store. */
class InMemoryTrustedClients(
    trusted: Set<String> = emptySet(),
) : TrustedClients {

    private val trusted = trusted.toMutableSet()
    private val pending = mutableSetOf<String>()
    private val rejected = mutableSetOf<String>()

    override fun isTrusted(clientFingerprint: String) = clientFingerprint in trusted
    override fun isPending(clientFingerprint: String) = clientFingerprint in pending
    override fun isRejected(clientFingerprint: String) = clientFingerprint in rejected

    override fun markPending(clientFingerprint: String) {
        if (clientFingerprint !in trusted) pending += clientFingerprint
    }

    override fun trust(clientFingerprint: String) {
        pending -= clientFingerprint
        rejected -= clientFingerprint
        trusted += clientFingerprint
    }

    override fun reject(clientFingerprint: String) {
        pending -= clientFingerprint
        trusted -= clientFingerprint
        rejected += clientFingerprint
    }

    override fun revoke(clientFingerprint: String) {
        pending -= clientFingerprint
        trusted -= clientFingerprint
        rejected -= clientFingerprint
    }

    override fun trustedFingerprints(): Set<String> = trusted.toSet()
}
