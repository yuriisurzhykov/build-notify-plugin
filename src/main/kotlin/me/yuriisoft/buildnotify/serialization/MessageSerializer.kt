package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Stateless object. ISP: only encode/decode, no other side responsibilities.
 */
object MessageSerializer {
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(message: WsMessage) = json.encodeToString(message)

    fun decode(message: String): WsMessage = json.decodeFromString(message)
}