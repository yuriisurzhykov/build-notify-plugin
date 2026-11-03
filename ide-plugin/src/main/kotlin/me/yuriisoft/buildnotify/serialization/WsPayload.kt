package me.yuriisoft.buildnotify.serialization

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
 * How to add a new message type:
 *   1. Add a data class that extends WsPayload.
 *   2. Annotate with @Serializable and @SerialName("namespace.name").
 *   3. Done — no registration, no factory, no changes to the serializer.
 */
@Serializable
sealed class WsPayload
