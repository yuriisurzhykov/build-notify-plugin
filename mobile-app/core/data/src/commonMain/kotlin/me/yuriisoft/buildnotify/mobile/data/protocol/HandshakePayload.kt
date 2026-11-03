package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent by the server immediately after the client connects.
 * Lets the client verify protocol compatibility before processing any event.
 *
 * Client behaviour:
 *   - protocolVersion > own supported version  → "please update the app"
 *   - protocolVersion < own minimum supported  → "please update the plugin"
 *   - otherwise → proceed normally
 */
@Serializable
@SerialName("sys.handshake")
data class HandshakePayload(
    @SerialName("version")
    val protocolVersion: Int = WsEnvelope.PROTOCOL_VERSION,
    val instanceId: String,
    val capabilities: Set<Capability> = emptySet(),
    /** SHA-256 fingerprint of the server TLS certificate, e.g. "AB:CD:EF:...". Null when TLS is off. */
    val certFingerprint: String? = null,
    /** Human-readable name for the IDE instance, e.g. "Android Studio — MyProject". */
    val deviceName: String = "",
) : WsPayload()
