package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model

data class BuildLogRecord(
    val buildId: String,
    val timestamp: Long,
    val message: String,
    val kind: LogRecordKind,
)
