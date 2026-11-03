package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent by the client after receiving [HandshakePayload] and completing TLS pairing.
 * Carries client identity so the server can display the device in its UI and
 * respond with [BuildSnapshotPayload] to synchronise state.
 */
@Serializable
@SerialName("sys.hello")
data class HelloPayload(
    val deviceName: String,
    val platform: String,
    val appVersion: String,
) : WsPayload()
