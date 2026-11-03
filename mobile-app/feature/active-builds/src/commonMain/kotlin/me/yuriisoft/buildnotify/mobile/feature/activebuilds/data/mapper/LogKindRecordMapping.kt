package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticSeverity

internal fun LogKind.toRecordKind(): LogRecordKind = when (this) {
    LogKind.TASK    -> LogRecordKind.TASK
    LogKind.WARNING -> LogRecordKind.WARNING
    LogKind.ERROR   -> LogRecordKind.ERROR
}

internal fun LogRecordKind.toDomain(): LogKind = when (this) {
    LogRecordKind.TASK    -> LogKind.TASK
    LogRecordKind.WARNING -> LogKind.WARNING
    LogRecordKind.ERROR   -> LogKind.ERROR
}

internal fun DiagnosticSeverity.toRecordKind(): LogRecordKind = logKind.toRecordKind()
