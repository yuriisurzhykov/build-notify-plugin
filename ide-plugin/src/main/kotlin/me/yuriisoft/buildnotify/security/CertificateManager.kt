package me.yuriisoft.buildnotify.security

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * Auto-generates and persists a self-signed TLS certificate for the WebSocket server.
 *
 * Storage layout under `<IDE config>/buildnotify/`:
 *   - `server.jks` — JKS keystore holding the RSA key pair + self-signed cert
 *   - `server.pwd` — randomly generated keystore password (single line)
 *
 * Certificate generation delegates to the JDK's `keytool` binary (always present in JBR),
 * avoiding any dependency on internal `sun.security.x509` APIs or third-party libraries.
 *
 * On first access (or when the stored keystore is missing/corrupt), a fresh certificate
 * is generated automatically. Advanced users can still override via `keystorePath` in settings.
 */
@Service(Service.Level.APP)
class CertificateManager {

    private val logger = thisLogger()

    private val configDir: Path =
        Path.of(PathManager.getConfigPath(), CONFIG_SUBDIRECTORY)

    private val keystorePath: Path = configDir.resolve(KEYSTORE_FILENAME)
    private val passwordPath: Path = configDir.resolve(PASSWORD_FILENAME)

    @Volatile
    private var cached: CertificateBundle? = null

    fun sslContext(): SSLContext? = bundle()?.sslContext

    fun fingerprint(): String? = bundle()?.fingerprint

    private fun bundle(): CertificateBundle? {
        cached?.let { return it }

        synchronized(this) {
            cached?.let { return it }

            return runCatching { loadOrGenerate() }
                .onFailure { e -> logger.error("Certificate setup failed", e) }
                .getOrNull()
                .also { cached = it }
        }
    }

    private fun loadOrGenerate(): CertificateBundle {
        Files.createDirectories(configDir)

        if (Files.isRegularFile(keystorePath) && Files.isRegularFile(passwordPath)) {
            return runCatching { loadExisting() }
                .onFailure { e ->
                    logger.warn("Stored keystore is corrupt; regenerating: ${e.message}")
                    Files.deleteIfExists(keystorePath)
                    Files.deleteIfExists(passwordPath)
                }
                .getOrElse { generate() }
        }

        return generate()
    }

    private fun loadExisting(): CertificateBundle {
        val password = Files.readString(passwordPath).trim().toCharArray()
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        Files.newInputStream(keystorePath).use { keyStore.load(it, password) }

        val cert = keyStore.getCertificate(KEY_ALIAS) as X509Certificate
        cert.checkValidity()

        return CertificateBundle(
            sslContext = buildSslContext(keyStore, password),
            fingerprint = sha256Fingerprint(cert),
        )
    }

    private fun generate(): CertificateBundle {
        logger.info("Generating self-signed TLS certificate via keytool...")

        val password = generatePassword()
        val passwordStr = String(password)

        runKeytool(
            "-genkeypair",
            "-alias", KEY_ALIAS,
            "-keyalg", KEY_ALGORITHM,
            "-keysize", KEY_SIZE.toString(),
            "-sigalg", SIGNATURE_ALGORITHM,
            "-validity", VALIDITY_DAYS.toString(),
            "-storetype", KEYSTORE_TYPE,
            "-keystore", keystorePath.toAbsolutePath().toString(),
            "-storepass", passwordStr,
            "-keypass", passwordStr,
            "-dname", DNAME,
        )

        Files.writeString(passwordPath, passwordStr)

        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        Files.newInputStream(keystorePath).use { keyStore.load(it, password) }

        val cert = keyStore.getCertificate(KEY_ALIAS) as X509Certificate
        logger.info("Self-signed certificate stored at $keystorePath (valid until ${cert.notAfter})")

        return CertificateBundle(
            sslContext = buildSslContext(keyStore, password),
            fingerprint = sha256Fingerprint(cert),
        )
    }

    companion object {

        private const val CONFIG_SUBDIRECTORY = "buildnotify"
        private const val KEYSTORE_FILENAME = "server.jks"
        private const val PASSWORD_FILENAME = "server.pwd"
        private const val KEYSTORE_TYPE = "JKS"
        private const val KEY_ALIAS = "buildnotify"
        private const val KEY_ALGORITHM = "RSA"
        private const val KEY_SIZE = 2048
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
        private const val VALIDITY_DAYS = 3650
        private const val PASSWORD_LENGTH = 32
        private const val KEYTOOL_TIMEOUT_SEC = 30L
        private const val DNAME = "CN=BuildNotify, O=BuildNotify Plugin, L=Local"

        @JvmStatic
        fun getInstance(): CertificateManager = service()

        private fun buildSslContext(keyStore: KeyStore, password: CharArray): SSLContext {
            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, password)

            return SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, null, null)
            }
        }

        private fun sha256Fingerprint(cert: X509Certificate): String =
            MessageDigest.getInstance("SHA-256")
                .digest(cert.encoded)
                .joinToString(":") { "%02X".format(it) }

        private fun generatePassword(): CharArray {
            val random = SecureRandom()
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return CharArray(PASSWORD_LENGTH) { chars[random.nextInt(chars.length)] }
        }

        private fun resolveKeytool(): Path {
            val javaHome = Path.of(System.getProperty("java.home"))
            val bin = javaHome.resolve("bin")
            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val executable = if (isWindows) "keytool.exe" else "keytool"
            val keytool = bin.resolve(executable)

            require(Files.isExecutable(keytool)) {
                "keytool not found at $keytool (java.home=$javaHome)"
            }
            return keytool
        }

        private fun runKeytool(vararg args: String) {
            val keytool = resolveKeytool()
            val command = listOf(keytool.toAbsolutePath().toString()) + args

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val completed = process.waitFor(KEYTOOL_TIMEOUT_SEC, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().readText().trim()

            if (!completed) {
                process.destroyForcibly()
                throw IllegalStateException("keytool timed out after ${KEYTOOL_TIMEOUT_SEC}s")
            }

            val exitCode = process.exitValue()
            if (exitCode != 0) {
                throw IllegalStateException("keytool failed (exit=$exitCode): $output")
            }
        }
    }

    private data class CertificateBundle(
        val sslContext: SSLContext,
        val fingerprint: String,
    )
}
