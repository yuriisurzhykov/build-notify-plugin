package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BuildLogsContent(
    state: BuildLogsUiState,
    onIntent: (BuildLogsIntent) -> Unit,
    onBack: () -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenSurface(modifier.fillMaxSize().safeDrawingPadding()) {
        LogsTopBar(state, onBack, onCopied)
        when (state) {
            is BuildLogsUiState.Loading  -> Centered { BouncingDots(color = GraphiteTheme.colors.running.main) }
            is BuildLogsUiState.NotFound -> Centered { NotFound() }
            is BuildLogsUiState.Loaded   -> LoadedLogs(state, onIntent)
        }
    }
}

@Composable
private fun NotFound() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        modifier = Modifier.padding(GraphiteTheme.spacing.xxl),
    ) {
        Text(
            stringResource(Res.string.logs_not_found_title),
            style = GraphiteTheme.typography.headline,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.logs_not_found_body),
            style = GraphiteTheme.typography.bodySmall,
            color = GraphiteTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
