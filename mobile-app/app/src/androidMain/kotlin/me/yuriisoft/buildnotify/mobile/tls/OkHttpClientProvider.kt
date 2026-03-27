package me.yuriisoft.buildnotify.mobile.tls

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import me.yuriisoft.buildnotify.mobile.network.client.HttpClientProvider
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Android [HttpClientProvider] that configures OkHttp with optional
 * certificate pinning for TOFU.
 *
 * When [provide] receives a non-null fingerprint, a custom
 * [BuildNotifyTrustManager] is installed that rejects any server
 * certificate whose SHA-256 fingerprint does not match.
 */
class OkHttpClientProvider : HttpClientProvider {

    private val cache = mutableMapOf<String, HttpClient>()

    override fun provide(fingerprint: String?): HttpClient {
        val key = fingerprint ?: PLAIN_KEY
        return cache.getOrPut(key) { buildClient(fingerprint) }
    }

    override fun release(fingerprint: String?) {
        val key = fingerprint ?: PLAIN_KEY
        cache.remove(key)?.close()
    }

    private fun buildClient(fingerprint: String?): HttpClient {
        val trustManager: X509TrustManager? = fingerprint?.let {
            BuildNotifyTrustManager(it)
        }

        return HttpClient(OkHttp) {
            engine {
                config {
                    if (trustManager != null) {
                        val sslContext = SSLContext.getInstance("TLS").apply {
                            init(null, arrayOf(trustManager), SecureRandom())
                        }
                        sslSocketFactory(sslContext.socketFactory, trustManager)
                        hostnameVerifier { _, _ -> true }
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
    }

    private companion object {
        const val PLAIN_KEY = "__plain__"
        const val PING_INTERVAL_MS = 15_000L
        const val CONNECT_TIMEOUT_MS = 10_000L
        const val SOCKET_TIMEOUT_MS = 60_000L
    }
}