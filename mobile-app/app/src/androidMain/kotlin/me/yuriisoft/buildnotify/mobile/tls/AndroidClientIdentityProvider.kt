package me.yuriisoft.buildnotify.mobile.tls

import me.yuriisoft.buildnotify.mobile.network.tls.ClientIdentityProvider

/**
 * Android implementation of [ClientIdentityProvider] backed by
 * [ClientCertificateManager].
 *
 * Delegates directly to [ClientCertificateManager.fingerprint], which reads
 * the self-signed certificate from the Android Keystore and returns its
 * SHA-256 fingerprint in colon-separated uppercase hex format.
 *
 * @param certManager must be initialised via [ClientCertificateManager.ensureInitialized]
 *   before the first call to [fingerprint].
 */
class AndroidClientIdentityProvider(
    private val certManager: ClientCertificateManager,
) : ClientIdentityProvider {

    override fun fingerprint(): String = certManager.fingerprint()
}
