package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.yuriisoft.buildnotify.build.model.BuildResult

/** Final build result with aggregated statistics, issues, and timing. */
@Serializable
@SerialName("build.result")
data class BuildResultPayload(
    val result: BuildResult,
) : WsPayload()
