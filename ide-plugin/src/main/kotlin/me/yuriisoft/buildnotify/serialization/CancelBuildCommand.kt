package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cmd.cancel_build")
data class CancelBuildCommand(
    val buildId: String,
) : WsPayload()
