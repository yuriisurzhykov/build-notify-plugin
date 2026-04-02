package me.yuriisoft.buildnotify.mobile.network.tls

/**
 * Provides the SHA-256 fingerprint of this device's client certificate.
 *
 * The fingerprint is stable for the lifetime of the app installation and is
 * used by [PairingCoordinator][me.yuriisoft.buildnotify.mobile.network.pairing.PairingCoordinator]
 * to derive the 6-digit PIN that both devices must display simultaneously.
 *
 * Platform implementations wrap the platform-specific key-management facility:
 * - Android: `ClientCertificateManager` (Android Keystore)
 * - iOS: `ClientIdentityManager` (Keychain / Secure Enclave)
 *
 * ### Thread safety
 *
 * Implementations must be safe to call from any thread. The fingerprint is
 * typically read on a coroutine dispatcher during the pairing flow.
 */
interface ClientIdentityProvider {

    /**
     * Returns the SHA-256 fingerprint of the client certificate in
     * colon-separated uppercase hex format (e.g. `"AB:CD:EF:12:34:..."`).
     *
     * @throws IllegalStateException if the client identity has not been
     *   initialised yet (should never happen in practice because platform
     *   entry points call `ensureInitialized()` before the DI graph is built).
     */
    fun fingerprint(): String
}
