package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import me.yuriisoft.buildnotify.mobile.designsystem.components.HistoryRow
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_issue_counts
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.history_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.RunningBuildUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RunningHistoryRow(build: RunningBuildUi, showDivider: Boolean, onIntent: (ActiveBuildsIntent) -> Unit) {
    HistoryRow(
        tone = BuildTone.Running,
        statusDescription = stringResource(BuildStatusAppearance.RUNNING.label),
        title = build.projectName,
        subtitle = build.currentTask ?: stringResource(Res.string.history_running),
        duration = formatTimer(rememberElapsedMs(build.startedAt)),
        time = relativeTime(build.startedAt),
        showDivider = showDivider,
        onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
    )
}

@Composable
internal fun FinishedHistoryRow(build: FinishedBuildUi, showDivider: Boolean, onIntent: (ActiveBuildsIntent) -> Unit) {
    val hasIssues = build.errorCount > 0 || build.warningCount > 0
    HistoryRow(
        tone = BuildStatusAppearance.of(build.status).tone,
        statusDescription = stringResource(BuildStatusAppearance.of(build.status).label),
        title = build.projectName,
        subtitle = if (hasIssues) {
            stringResource(Res.string.build_issue_counts, build.errorCount, build.warningCount)
        } else {
            stringResource(BuildStatusAppearance.of(build.status).label)
        },
        duration = formatDuration(build.durationMs),
        time = relativeTime(build.startedAt + build.durationMs),
        showDivider = showDivider,
        onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
    )
}
