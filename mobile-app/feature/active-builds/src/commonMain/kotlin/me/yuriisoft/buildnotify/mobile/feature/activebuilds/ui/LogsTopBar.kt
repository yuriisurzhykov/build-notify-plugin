package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.IconButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_back
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_unknown_project
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_copy
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildStatusUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogsTopBar(state: BuildLogsUiState, onBack: () -> Unit, onCopied: () -> Unit) {
    val loaded = state as? BuildLogsUiState.Loaded
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            GraphiteIcons.ArrowBack,
            contentDescription = stringResource(Res.string.action_back),
            onClick = onBack,
            tint = GraphiteTheme.colors.content,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                loaded?.projectName ?: stringResource(Res.string.logs_title),
                style = GraphiteTheme.typography.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (loaded != null) StatusLine(loaded.status)
        }
        when (val status = loaded?.status) {
            is BuildStatusUi.Running  -> BuildStatusPill(BuildStatusAppearance.RUNNING)
            is BuildStatusUi.Finished -> BuildStatusPill(BuildStatusAppearance.of(status.status))
            is BuildStatusUi.Unknown,
            null                      -> Unit
        }
        if (loaded != null && loaded.totalLines > 0) {
            IconButton(
                GraphiteIcons.Copy,
                contentDescription = stringResource(Res.string.logs_copy),
                onClick = {
                    clipboard.setText(AnnotatedString(loaded.lines.joinToString("\n") { it.text }))
                    onCopied()
                },
            )
        }
    }
}

@Composable
private fun StatusLine(status: BuildStatusUi) {
    val text = when (status) {
        is BuildStatusUi.Running  -> {
            val elapsed = formatTimer(rememberElapsedMs(status.startedAt))
            status.currentTask?.let { "$elapsed · $it" } ?: elapsed
        }

        is BuildStatusUi.Finished -> formatDuration(status.durationMs)
        is BuildStatusUi.Unknown  -> stringResource(Res.string.build_unknown_project)
    }
    Text(text, style = GraphiteTheme.typography.mono, color = GraphiteTheme.colors.contentSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
