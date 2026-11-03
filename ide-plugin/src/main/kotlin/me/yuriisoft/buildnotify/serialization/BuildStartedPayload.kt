package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("build.started")
data class BuildStartedPayload(
    val buildId: String,
    val projectName: String,
) : WsPayload()
