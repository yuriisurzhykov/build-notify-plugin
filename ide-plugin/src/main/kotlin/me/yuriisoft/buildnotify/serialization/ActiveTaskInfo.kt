package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.Serializable
import me.yuriisoft.buildnotify.build.pipeline.TaskStatus

@Serializable
data class ActiveTaskInfo(
    val taskPath: String,
    val status: TaskStatus,
)
