package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

enum class DiagnosticSeverity(val logKind: LogKind) {
    WARNING(LogKind.WARNING),
    ERROR(LogKind.ERROR),
}