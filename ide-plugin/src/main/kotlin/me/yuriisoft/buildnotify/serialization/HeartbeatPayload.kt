package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("sys.heartbeat")
data class HeartbeatPayload(
    val serverTime: Long = System.currentTimeMillis(),
) : WsPayload()
