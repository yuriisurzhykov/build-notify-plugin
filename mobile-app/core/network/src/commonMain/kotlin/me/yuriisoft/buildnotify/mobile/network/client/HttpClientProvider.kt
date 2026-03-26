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
}
