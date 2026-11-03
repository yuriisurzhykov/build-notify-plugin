package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

/** The newest [limit] lines of [buildId] whose kind is in [kinds]. */
data class LogQuery(
    val buildId: String,
    val kinds: Set<LogKind>,
    val limit: Int,
)
