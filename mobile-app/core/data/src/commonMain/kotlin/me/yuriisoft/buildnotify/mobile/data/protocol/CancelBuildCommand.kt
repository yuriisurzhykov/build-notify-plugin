package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cmd.cancel_build")
data class CancelBuildCommand(
    val buildId: String,
) : WsPayload()
