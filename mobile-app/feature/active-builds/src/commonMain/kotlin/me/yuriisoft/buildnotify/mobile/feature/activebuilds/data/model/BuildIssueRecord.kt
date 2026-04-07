package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model

data class BuildIssueRecord(
    val buildId: String,
    val message: String,
    val filePath: String?,
    val line: Int?,
    val severity: IssueSeverity,
)
