package me.yuriisoft.buildnotify.mobile.network.connection

/**
 * A Build Notify plugin instance resolved to a connectable endpoint.
 *
 * [name] is the human-readable service name (e.g. "MyMacBook IDE").
 * [host] is the resolved IP address or hostname.
 * [port] is the WebSocket server port.
 * [scheme] is `"wss"` when the server advertises TLS, `"ws"` otherwise.
 * [fingerprint] is the SHA-256 certificate fingerprint from the mDNS TXT
 * record (`fp` key), used for TOFU pin verification. `null` when TLS is
 * not advertised.
 *
 * Lives in :core:network so that both the connection layer and feature
 * modules share one definition without circular dependencies.
 */
data class DiscoveredHost(
    val name: String,
    val host: String,
    val port: Int,
    val scheme: String = "ws",
    val fingerprint: String? = null,
) {
    val isSecure: Boolean get() = scheme == "wss"
}
