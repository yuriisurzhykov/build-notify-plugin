package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry

/** What the ViewModel knows about an open hand-off sheet. */
data class HandoffRequest(
    val buildId: String,
    val includeLog: Boolean,
    /** The tail of the log, at most [HandoffPrompt.MAX_LOG_LINES] lines. */
    val logs: List<BuildLogEntry>,
    val totalLines: Int = logs.size,
)
