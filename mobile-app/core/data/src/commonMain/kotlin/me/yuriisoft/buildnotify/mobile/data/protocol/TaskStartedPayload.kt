package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Gradle task has started executing.
 * The client may show an animated indicator until the matching
 * [TaskFinishedPayload] arrives for the same [buildId] + [taskPath].
 */
@Serializable
@SerialName("build.task_started")
data class TaskStartedPayload(
    val buildId: String,
    val projectName: String,
    val taskPath: String,
) : WsPayload()
