package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

/** Content of the "hand off to an agent" sheet for one failed build. */
@Immutable
data class HandoffUi(
    val buildId: String,
    val projectName: String,
    val errorCount: Int,
    /** Ready-to-send message: errors with locations, optionally the log. */
    val prompt: String,
    val includeLog: Boolean,
    val logLineCount: Int,
)
