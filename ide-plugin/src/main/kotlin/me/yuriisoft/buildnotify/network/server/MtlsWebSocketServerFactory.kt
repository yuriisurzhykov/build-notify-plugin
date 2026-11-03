package me.yuriisoft.buildnotify.network.server

import org.java_websocket.SSLSocketChannel2
import org.java_websocket.server.DefaultSSLWebSocketServerFactory
import java.nio.channels.ByteChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.security.SecureRandom
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * [DefaultSSLWebSocketServerFactory] subclass that enables mutual TLS by
 * setting `needClientAuth = true` on every accepted connection's [javax.net.ssl.SSLEngine].
 *
 * ## Why we override [wrapChannel]
 *
 * [DefaultSSLWebSocketServerFactory.wrapChannel] creates the [javax.net.ssl.SSLEngine]
 * internally and immediately passes it to [SSLSocketChannel2] — there is no
 * post-creation hook. Overriding [wrapChannel] is the only way to control engine
 * parameters before the TLS handshake starts.
 *
 * ## Why [KeyManager]s are passed explicitly
 *
 * [SSLContext] has no `getKeyManagers()` API. Once initialised, its internal key
 * material is opaque. Building a composite context (server key + client TrustManager)
 * therefore requires the [KeyManager]s to be supplied from the outside.
 * [me.yuriisoft.buildnotify.security.CertificateBundle] now carries them for
 * exactly this purpose.
 *
 * ## Cipher-suite workaround
 *
 * The removal of `TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256` is copied verbatim from
 * [DefaultSSLWebSocketServerFactory] — see https://github.com/TooTallNate/Java-WebSocket/issues/466.
 * It must be preserved here because we bypass the parent's [wrapChannel].
 *
 * ## Client certificates are mandatory
 *
 * `needClientAuth = true`: a client that presents no certificate fails the TLS
 * handshake, and an unknown certificate is held "pending" by
 * [ClientToFuTrustManager.checkClientTrusted] until the user approves the PIN.
 * Only paired devices ever complete a handshake. (The earlier `wantClientAuth`
 * let anonymous LAN clients connect and read every build.)
 *
 * @param serverKeyManagers  Key material for the server certificate, taken from
 *                           [me.yuriisoft.buildnotify.security.CertificateBundle.keyManagers].
 * @param clientTrustManager TOFU [X509TrustManager] for client-cert verification.
 * @param exec               TLS I/O executor — defaults to single scheduled thread,
 *                           matching [DefaultSSLWebSocketServerFactory]'s default.
 */
class MtlsWebSocketServerFactory(
    serverKeyManagers: Array<KeyManager>,
    clientTrustManager: X509TrustManager,
    exec: ExecutorService = Executors.newSingleThreadScheduledExecutor(),
) : DefaultSSLWebSocketServerFactory(
    buildMtlsContext(serverKeyManagers, clientTrustManager),
    exec,
) {

    override fun wrapChannel(channel: SocketChannel?, key: SelectionKey?): ByteChannel {
        val engine = sslcontext.createSSLEngine().apply {
            val ciphers = enabledCipherSuites.toMutableList()
            ciphers.remove("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
            enabledCipherSuites = ciphers.toTypedArray()

            useClientMode = false
            // Required, not optional: a client without a certificate fails the handshake.
            needClientAuth = true
        }
        return SSLSocketChannel2(channel, engine, exec, key)
    }

    private companion object {

        /**
         * Produces an [SSLContext] that contains both the server's [KeyManager]s
         * (so the server can present its certificate to the client) and the
         * [clientTrustManager] (so the server can verify the client's certificate).
         */
        fun buildMtlsContext(
            serverKeyManagers: Array<KeyManager>,
            clientTrustManager: X509TrustManager,
        ): SSLContext = SSLContext.getInstance("TLS").apply {
            init(
                /* km = */ serverKeyManagers,
                /* tm = */ arrayOf(clientTrustManager),
                /* random = */ SecureRandom(),
            )
        }
    }
}