package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

/** Which kinds of line the log screen shows; the filter runs in SQL, not over a list. */
enum class LogFilter(val kinds: Set<LogKind>) {
    ALL(LogKind.entries.toSet()),
    PROBLEMS(setOf(LogKind.WARNING, LogKind.ERROR)),
    ERRORS(setOf(LogKind.ERROR)),
    ;

    fun accepts(kind: LogKind): Boolean = kind in kinds
}
