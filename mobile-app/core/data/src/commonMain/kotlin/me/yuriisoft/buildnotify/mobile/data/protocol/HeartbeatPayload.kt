package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

/** Keep-alive ping from server. Client resets its "connection lost" timer on receipt. */
@Serializable
@SerialName("sys.heartbeat")
data class HeartbeatPayload(
    val serverTime: Long = Clock.System.now().toEpochMilliseconds(),
) : WsPayload()
