package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

data class DiagnosticEvent(
    val buildId: String,
    val severity: DiagnosticSeverity,
    val message: String,
    val filePath: String?,
    val line: Int?,
    val timestamp: Long,
) : BuildEvent
