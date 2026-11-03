package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Final build result with aggregated statistics, issues, and timing. */
@Serializable
@SerialName("build.result")
data class BuildResultPayload(
    val result: BuildResult,
) : WsPayload()
