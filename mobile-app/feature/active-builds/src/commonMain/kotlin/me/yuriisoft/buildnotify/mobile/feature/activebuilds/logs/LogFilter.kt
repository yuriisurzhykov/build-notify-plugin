package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

enum class LogFilter(val accepts: (LogKind) -> Boolean) {
    ALL({ true }),
    PROBLEMS({ it == LogKind.WARNING || it == LogKind.ERROR }),
    ERRORS({ it == LogKind.ERROR }),
}
