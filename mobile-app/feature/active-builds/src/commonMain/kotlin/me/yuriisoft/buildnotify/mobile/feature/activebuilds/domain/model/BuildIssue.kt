package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BuildIssue(
    val message: String,
    val filePath: String?,
    val line: Int?,
)
