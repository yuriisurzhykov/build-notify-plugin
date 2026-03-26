package me.yuriisoft.buildnotify.mobile.tls

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import me.yuriisoft.buildnotify.mobile.network.client.HttpClientProvider
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.serverTrust
import platform.Security.SecCertificateCopyData
import platform.Security.SecTrustGetCertificateAtIndex
import platform.Security.SecTrustGetCertificateCount

/**
 * iOS [HttpClientProvider] that configures the Darwin engine with optional
 * TOFU certificate pinning via `handleChallenge`.
 *
 * When [provide] receives a non-null fingerprint, the engine validates
 * that the leaf certificate's SHA-256 fingerprint matches the pinned value.
 */
class DarwinHttpClientProvider : HttpClientProvider {

    override fun provide(fingerprint: String?): HttpClient =
        HttpClient(Darwin) {
            engine {
                if (fingerprint != null) {
                    handleChallenge { _, _, challenge, completionHandler ->
                        handleTofuChallenge(fingerprint, challenge, completionHandler)
                    }
                }
            }
            install(WebSockets) {
                pingIntervalMillis = PING_INTERVAL_MS
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun handleTofuChallenge(
        expectedFingerprint: String,
        challenge: NSURLAuthenticationChallenge,
        completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit,
    ) {
        val method = challenge.protectionSpace.authenticationMethod
        if (method != NSURLAuthenticationMethodServerTrust) {
            completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
            return
        }

        val serverTrust = challenge.protectionSpace.serverTrust
        if (serverTrust == null) {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            return
        }

        val certCount = SecTrustGetCertificateCount(serverTrust)
        if (certCount == 0L) {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            return
        }

        val leafCert = SecTrustGetCertificateAtIndex(serverTrust, 0)
        if (leafCert == null) {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            return
        }

        @Suppress("UNCHECKED_CAST")
        val certData = SecCertificateCopyData(leafCert) as? NSData
        if (certData == null) {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
            return
        }

        val actual = certData.sha256Fingerprint()

        if (actual.equals(expectedFingerprint, ignoreCase = true)) {
            val credential = NSURLCredential.credentialForTrust(serverTrust)
            completionHandler(NSURLSessionAuthChallengeUseCredential, credential)
        } else {
            completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.sha256Fingerprint(): String {
        val input = bytes ?: return ""
        val len = length.toInt()
        if (len == 0) return ""

        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
        hash.usePinned { pinned ->
            CC_SHA256(input, len.toUInt(), pinned.addressOf(0))
        }
        return hash.joinToString(":") { "%02X".format(it.toInt() and 0xFF) }
    }

    private companion object {
        const val PING_INTERVAL_MS = 15_000L
        const val CONNECT_TIMEOUT_MS = 10_000L
        const val SOCKET_TIMEOUT_MS = 60_000L
    }
}
