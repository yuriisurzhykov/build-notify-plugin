package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model

data class BuildRecord(
    val buildId: String,
    val projectName: String,
    val status: BuildRecordStatus,
    val startedAt: Long,
    val currentTask: String?,
    val resultJson: String?,
)
