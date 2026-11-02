package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun logColor(kind: LogKind): Color = when (kind) {
    LogKind.TASK    -> BuildNotifyTheme.colors.content.onCode
    LogKind.WARNING -> BuildNotifyTheme.colors.status.warning.main
    LogKind.ERROR   -> BuildNotifyTheme.colors.status.error.main
}

/** One monospace log line; [singleLine] is used by the compact console on build cards. */
@Composable
internal fun LogLine(
    text: String,
    kind: LogKind,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
) {
    Text(
        text = TextResource.RawText(text),
        modifier = modifier,
        style = BuildNotifyTheme.typography.code.small,
        color = logColor(kind),
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
    )
}
