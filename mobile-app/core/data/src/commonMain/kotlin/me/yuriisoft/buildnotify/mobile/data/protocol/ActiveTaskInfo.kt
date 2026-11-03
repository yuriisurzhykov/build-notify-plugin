package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ActiveTaskInfo(
    val taskPath: String,
    val status: TaskStatus,
)
