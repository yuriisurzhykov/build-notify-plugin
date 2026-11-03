package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.Serializable

@Serializable
data class ActiveBuildInfo(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val tasks: List<ActiveTaskInfo>,
)
