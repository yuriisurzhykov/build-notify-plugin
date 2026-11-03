package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Immutable
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

@Immutable
data class BuildLogLineUi(
    /** Offset from the first line, e.g. `+12.4s`. */
    val offset: String,
    val text: String,
    val kind: LogKind,
)
