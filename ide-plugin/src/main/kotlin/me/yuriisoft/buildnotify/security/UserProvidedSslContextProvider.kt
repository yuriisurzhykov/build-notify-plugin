package me.yuriisoft.buildnotify.security

import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.settings.PluginSettingsState
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * Loads a TLS certificate from a user-provided JKS keystore.
 *
 * Configuration:
 *   - keystore path  → [PluginSettingsState] `keystorePath`  OR  env `BUILDNOTIFY_KEYSTORE_PATH`
 *   - password       → env `BUILDNOTIFY_KEYSTORE_PASSWORD`
 */
class UserProvidedSslContextProvider(
    private val settings: PluginSettingsState.State,
) : SslContextProvider {

    private val logger = thisLogger()

    override fun isApplicable(): Boolean =
        password() != null && resolveKeystoreStream() != null

    override fun provide(): CertificateBundle? {
        val password = password() ?: return null
        val stream = resolveKeystoreStream() ?: return null

        return stream.use { keystoreStream ->
            runCatching {
                val keyStore = KeyStore.getInstance("JKS")
                keyStore.load(keystoreStream, password)

                val cert = keyStore.getCertificate(KEY_ALIAS) as X509Certificate

                CertificateBundle(
                    sslContext = buildSslContext(keyStore, password),
                    fingerprint = sha256Fingerprint(cert),
                )
            }.onFailure { e ->
                logger.warn("User-provided keystore failed to load: ${e.message}")
            }.getOrNull()
        }
    }

    private fun password(): CharArray? =
        System.getenv("BUILDNOTIFY_KEYSTORE_PASSWORD")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.toCharArray()

    private fun resolveKeystoreStream(): InputStream? {
        val fromSettings = settings.keystorePath.trim()
        if (fromSettings.isNotEmpty()) {
            val path = Path.of(fromSettings)
            if (Files.isRegularFile(path)) return Files.newInputStream(path)
            logger.warn("Keystore path from settings not found: $fromSettings")
        }

        val fromEnv = System.getenv("BUILDNOTIFY_KEYSTORE_PATH")?.trim().orEmpty()
        if (fromEnv.isNotEmpty()) {
            val path = Path.of(fromEnv)
            if (Files.isRegularFile(path)) return Files.newInputStream(path)
            logger.warn("BUILDNOTIFY_KEYSTORE_PATH not found: $fromEnv")
        }

        return null
    }

    private companion object {
        const val KEY_ALIAS = "buildnotify"

        fun buildSslContext(keyStore: KeyStore, password: CharArray): SSLContext {
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, password)
            return SSLContext.getInstance("TLS").apply { init(kmf.keyManagers, null, null) }
        }

        fun sha256Fingerprint(cert: X509Certificate): String =
            MessageDigest.getInstance("SHA-256")
                .digest(cert.encoded)
                .joinToString(":") { "%02X".format(it) }
    }
}