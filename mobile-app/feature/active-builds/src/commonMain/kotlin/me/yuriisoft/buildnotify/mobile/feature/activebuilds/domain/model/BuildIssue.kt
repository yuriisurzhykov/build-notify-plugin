package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

data class BuildIssue(
    val message: String,
    val filePath: String?,
    val line: Int?,
)
