package me.yuriisoft.buildnotify.network.server

import me.yuriisoft.buildnotify.security.TrustedClients

/** Authorized = presented a certificate whose fingerprint is currently trusted. */
class TrustedClientAuthorizer(
    private val trustedClients: () -> TrustedClients,
) : SessionAuthorizer {

    override fun isAuthorized(clientFingerprint: String?): Boolean =
        clientFingerprint != null && trustedClients().isTrusted(clientFingerprint)
}
