package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class BuildIssueUi(
    val message: String,
    /** `path/File.kt:42`, or `null` for project-level diagnostics. */
    val location: String?,
)
