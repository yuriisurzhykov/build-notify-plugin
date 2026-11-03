package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Gradle task has started executing.
 *
 * The client may render an animated "running" indicator until the matching
 * [TaskFinishedPayload] arrives for the same [buildId] + [taskPath].
 */
@Serializable
@SerialName("build.task_started")
data class TaskStartedPayload(
    val buildId: String,
    val projectName: String,
    val taskPath: String,
) : WsPayload()
