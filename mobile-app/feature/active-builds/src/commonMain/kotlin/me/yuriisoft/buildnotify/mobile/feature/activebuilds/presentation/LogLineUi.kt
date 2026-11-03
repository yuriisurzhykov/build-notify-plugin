package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

@Immutable
data class LogLineUi(
    val text: String,
    val kind: LogKind,
)
