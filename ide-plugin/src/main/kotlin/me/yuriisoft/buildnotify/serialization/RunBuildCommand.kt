package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cmd.run_build")
data class RunBuildCommand(
    val projectName: String,
    val tasks: List<String> = emptyList(),
) : WsPayload()
