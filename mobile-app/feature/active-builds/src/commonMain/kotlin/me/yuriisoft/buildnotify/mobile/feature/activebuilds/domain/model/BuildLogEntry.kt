package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

/**
 * One log line. [id] is its position in the build's log (the store's row id): it grows
 * monotonically, so it is a stable key for lists and a cursor for paging.
 */
data class BuildLogEntry(
    val timestamp: Long,
    val message: String,
    val kind: LogKind,
    val id: Long = 0L,
)
