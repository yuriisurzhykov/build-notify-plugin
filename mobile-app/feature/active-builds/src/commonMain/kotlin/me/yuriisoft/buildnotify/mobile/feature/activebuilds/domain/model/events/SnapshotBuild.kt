package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

data class SnapshotBuild(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val currentTask: String?,
)
