package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent by the server immediately after a client connects (onOpen).
 * Lets the client verify protocol compatibility before processing any other event.
 *
 * Client behaviour:
 *   - if protocolVersion > its own supported version → show "please update the app"
 *   - if protocolVersion < its own minimum supported → show "please update the plugin"
 *   - otherwise proceed normally
 */
@Serializable
@SerialName("sys.handshake")
data class HandshakePayload(
    val protocolVersion: Int = WsEnvelope.PROTOCOL_VERSION,
    /**
     * Stable opaque ID for this IDE instance; changes only on full reinstall / new machine.
     * */
    val instanceId: String,
    /**
     * What this server can do right now; client hides UI for missing capabilities.
     * */
    val capabilities: Set<Capability> = emptySet(),
    /**
     * SHA-256 fingerprint of the server TLS certificate, e.g. "AB:CD:EF:...". Null when TLS is off.
     * */
    val certFingerprint: String? = null,
    /**
     * Human-readable name for the IDE instance, e.g. "Android Studio — MyProject".
     * */
    val deviceName: String = "",
) : WsPayload()
