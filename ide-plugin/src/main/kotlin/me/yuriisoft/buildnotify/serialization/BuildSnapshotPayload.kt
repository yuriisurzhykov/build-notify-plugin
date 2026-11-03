package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.yuriisoft.buildnotify.build.model.BuildResult

/**
 * Sent by the server immediately after receiving [HelloPayload].
 * Gives the client a consistent view of every build that is currently in-flight
 * so it does not miss events that started before it connected.
 */
@Serializable
@SerialName("build.snapshot")
data class BuildSnapshotPayload(
    val activeBuilds: List<ActiveBuildInfo>,
    val recentResults: List<BuildResult> = emptyList(),
) : WsPayload()
