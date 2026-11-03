package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.yuriisoft.buildnotify.build.pipeline.TaskStatus

/**
 * A Gradle task has finished executing.
 *
 * [status] tells the client the terminal outcome so it can color-code tasks
 * (e.g. grey for UP_TO_DATE, red for FAILED, green for SUCCESS).
 */
@Serializable
@SerialName("build.task_finished")
data class TaskFinishedPayload(
    val buildId: String,
    val projectName: String,
    val taskPath: String,
    val status: TaskStatus,
) : WsPayload()
