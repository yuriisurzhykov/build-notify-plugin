package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

data class BuildLogEntry(
    val timestamp: Long,
    val message: String,
    val kind: LogKind,
)
