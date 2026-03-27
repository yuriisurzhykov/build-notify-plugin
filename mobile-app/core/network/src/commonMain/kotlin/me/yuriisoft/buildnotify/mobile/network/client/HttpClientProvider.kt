package me.yuriisoft.buildnotify.mobile.network.client

import io.ktor.client.HttpClient

/**
 * Factory for a properly configured [HttpClient] used by the network layer.
 *
 * Platform implementations configure TLS trust (OkHttp / Darwin) and pin
 * the server certificate when a [fingerprint] is supplied (TOFU).
 *
 * [provide] without a fingerprint returns a default client that accepts
 * any server certificate — suitable for plain `ws://` connections.
 */
interface HttpClientProvider {

    fun provide(fingerprint: String? = null): HttpClient

    /**
     * Releases the cached [HttpClient] associated with [fingerprint].
     *
     * Call on explicit disconnect only — not on transient retry failures,
     * so the same client is reused across reconnect attempts.
     *
     * No-op by default; override only where client pooling is used (OkHttp).
     * Darwin engine manages its own session lifecycle and doesn't need this.
     */
    fun release(fingerprint: String?) {}
}
