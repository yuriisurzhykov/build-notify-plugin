package me.yuriisoft.buildnotify.mobile.tls

import me.yuriisoft.buildnotify.mobile.network.tls.ClientIdentityProvider

/**
 * iOS implementation of [ClientIdentityProvider] backed by
 * [ClientIdentityManager].
 *
 * Delegates to [ClientIdentityManager.fingerprint], which reads the
 * self-signed certificate from the iOS Keychain and returns its SHA-256
 * fingerprint in colon-separated uppercase hex format.
 *
 * @param identityManager must be initialised via
 *   [ClientIdentityManager.ensureInitialized] before the first call to
 *   [fingerprint].
 */
class IosClientIdentityProvider(
    private val identityManager: ClientIdentityManager,
) : ClientIdentityProvider {

    override fun fingerprint(): String =
        identityManager.fingerprint()
            ?: error("Client identity not initialised. Was ensureInitialized() called?")
}
