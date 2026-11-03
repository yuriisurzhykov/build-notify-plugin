package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Gradle task has finished executing.
 * [status] drives colour-coding in the UI (grey = UP_TO_DATE, red = FAILED, etc.).
 */
@Serializable
@SerialName("build.task_finished")
data class TaskFinishedPayload(
    val buildId: String,
    val projectName: String,
    val taskPath: String,
    val status: TaskStatus,
) : WsPayload()
