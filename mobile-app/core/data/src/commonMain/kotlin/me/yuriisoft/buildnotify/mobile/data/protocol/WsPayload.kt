package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed hierarchy of all message payloads.
 *
 * Naming convention for @SerialName  →  "<namespace>.<action>"
 *   sys.*      — system / connection lifecycle
 *   build.*    — IDE build events, server → client
 *   cmd.*      — commands from client → server, and their results back
 *   agent.*    — AI-agent events, reserved for future use
 *
 * To add a new message type:
 *   1. Add a data class extending [WsPayload].
 *   2. Annotate with @Serializable and @SerialName("namespace.name").
 *   3. Done — no registration, no factory, no other changes needed.
 *
 * Mirror of the plugin's WsPayload — intentionally kept identical so the
 * two ends can evolve in lockstep without a separate shared artifact (YAGNI).
 */
@Serializable
sealed class WsPayload
