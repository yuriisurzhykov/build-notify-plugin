package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.Serializable

@Serializable
data class ActiveBuildInfo(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val tasks: List<ActiveTaskInfo>,
)
