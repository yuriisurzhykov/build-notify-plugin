package me.yuriisoft.buildnotify.build.model

import kotlinx.serialization.Serializable

@Serializable
data class BuildIssue(
    val filePath: String,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: Severity,
) {

    @Serializable
    enum class Severity {
        ERROR, WARNING
    }
}