package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogTone
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

/** How a log line of this kind reads in a Graphite console. */
internal fun LogKind.tone(): LogTone = when (this) {
    LogKind.TASK    -> LogTone.Default
    LogKind.WARNING -> LogTone.Warning
    LogKind.ERROR   -> LogTone.Error
}
