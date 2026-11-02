package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_retry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_stop
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_issue_counts
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_more_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_preparing
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_section_recent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_section_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_stream_error
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.RunningBuildUi
import me.yuriisoft.buildnotify.mobile.ui.components.button.GhostButton
import me.yuriisoft.buildnotify.mobile.ui.components.button.SecondaryButton
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.components.layout.CodeSurface
import me.yuriisoft.buildnotify.mobile.ui.components.layout.ElevatedSurface
import me.yuriisoft.buildnotify.mobile.ui.components.progress.CircularProgress
import me.yuriisoft.buildnotify.mobile.ui.components.progress.LinearProgress
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.circular.CircularProgressMode
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.linear.LinearProgressMode
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import me.yuriisoft.buildnotify.mobile.ui.resource.textResource
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun ActiveBuildsContent(
    state: ActiveBuildsUiState,
    onIntent: (ActiveBuildsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = BuildNotifyTheme.dimensions.layout
    val spacing = BuildNotifyTheme.dimensions.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Text(
            text = textResource(Res.string.builds_title),
            style = BuildNotifyTheme.typography.headingLarge,
            color = BuildNotifyTheme.colors.content.primary,
            modifier = Modifier.padding(
                horizontal = layout.contentPadding,
                vertical = spacing.large,
            ),
        )

        when (state) {
            ActiveBuildsUiState.Loading -> CenteredProgress()
            ActiveBuildsUiState.Empty   -> EmptyBuilds()
            is ActiveBuildsUiState.Loaded -> LoadedBuilds(state, onIntent)
        }
    }
}

@Composable
private fun CenteredProgress() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgress(mode = CircularProgressMode.Indeterminate())
    }
}

@Composable
private fun EmptyBuilds() {
    val spacing = BuildNotifyTheme.dimensions.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(BuildNotifyTheme.dimensions.layout.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = textResource(Res.string.builds_empty_title),
                style = BuildNotifyTheme.typography.titleMedium,
                color = BuildNotifyTheme.colors.content.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.tiny))
            Text(
                text = textResource(Res.string.builds_empty_body),
                style = BuildNotifyTheme.typography.bodyMedium,
                color = BuildNotifyTheme.colors.content.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LoadedBuilds(
    state: ActiveBuildsUiState.Loaded,
    onIntent: (ActiveBuildsIntent) -> Unit,
) {
    val layout = BuildNotifyTheme.dimensions.layout
    val spacing = BuildNotifyTheme.dimensions.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = layout.contentPadding,
            end = layout.contentPadding,
            bottom = spacing.xxLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.regular),
    ) {
        if (state.streamFailed) {
            item(key = "stream_error", contentType = "banner") {
                StreamErrorBanner(onRetry = { onIntent(ActiveBuildsIntent.RetryStream) })
            }
        }
        if (state.running.isNotEmpty()) {
            item(key = "header_running", contentType = "header") {
                SectionHeader(textResource(Res.string.builds_section_running))
            }
            items(state.running, key = { "running_${it.buildId}" }, contentType = { "running" }) {
                RunningBuildCard(build = it, onIntent = onIntent)
            }
        }
        if (state.finished.isNotEmpty()) {
            item(key = "header_recent", contentType = "header") {
                SectionHeader(textResource(Res.string.builds_section_recent))
            }
            items(state.finished, key = { "finished_${it.buildId}" }, contentType = { "finished" }) {
                FinishedBuildCard(build = it, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: TextResource) {
    Text(
        text = title,
        style = BuildNotifyTheme.typography.labelLarge,
        color = BuildNotifyTheme.colors.content.secondary,
        modifier = Modifier.padding(top = BuildNotifyTheme.dimensions.spacing.small),
    )
}

@Composable
private fun StreamErrorBanner(onRetry: () -> Unit) {
    val error = BuildNotifyTheme.colors.status.error
    ElevatedSurface(
        modifier = Modifier.fillMaxWidth(),
        color = error.container,
        contentColor = error.onContainer,
    ) {
        Row(
            modifier = Modifier.padding(BuildNotifyTheme.dimensions.spacing.regular),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = textResource(Res.string.builds_stream_error),
                style = BuildNotifyTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            GhostButton(onClick = onRetry, contentColor = error.onContainer) {
                Text(text = textResource(Res.string.action_retry))
            }
        }
    }
}

@Composable
private fun CardHeader(
    title: String,
    subtitle: String,
    status: FinishStatus?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = TextResource.RawText(title),
                style = BuildNotifyTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = TextResource.RawText(subtitle),
                style = BuildNotifyTheme.typography.bodySmall,
                color = BuildNotifyTheme.colors.content.onElevatedVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(BuildNotifyTheme.dimensions.spacing.small))
        BuildStatusBadge(status = status)
    }
}

@Composable
private fun RunningBuildCard(
    build: RunningBuildUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
) {
    val spacing = BuildNotifyTheme.dimensions.spacing
    val elapsed = rememberElapsedMs(build.startedAt)

    ElevatedSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.regular),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            CardHeader(
                title = build.projectName,
                subtitle = formatDuration(elapsed),
                status = null,
            )

            Text(
                text = build.currentTask?.let { TextResource.RawText(it) }
                    ?: textResource(Res.string.build_preparing),
                style = BuildNotifyTheme.typography.code.small,
                color = BuildNotifyTheme.colors.status.info.main,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LinearProgress(mode = LinearProgressMode.Indeterminate())

            if (build.recentLogs.isNotEmpty()) {
                CodeSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(spacing.small)) {
                        build.recentLogs.forEach { line ->
                            LogLine(text = line.text, kind = line.kind, singleLine = true)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                SecondaryButton(
                    onClick = { onIntent(ActiveBuildsIntent.StopBuild(build.buildId)) },
                    contentColor = BuildNotifyTheme.colors.status.error.main,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = textResource(Res.string.action_stop))
                }
                SecondaryButton(
                    onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = textResource(Res.string.action_logs))
                }
            }
        }
    }
}

@Composable
private fun FinishedBuildCard(
    build: FinishedBuildUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
) {
    val spacing = BuildNotifyTheme.dimensions.spacing

    ElevatedSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
    ) {
        Column(
            modifier = Modifier.padding(spacing.regular),
            verticalArrangement = Arrangement.spacedBy(spacing.tiny),
        ) {
            CardHeader(
                title = build.projectName,
                subtitle = formatDuration(build.durationMs),
                status = build.status,
            )

            if (build.errorCount > 0 || build.warningCount > 0) {
                Text(
                    text = textResource(
                        Res.string.build_issue_counts,
                        build.errorCount,
                        build.warningCount,
                    ),
                    style = BuildNotifyTheme.typography.labelSmall,
                    color = BuildNotifyTheme.colors.content.onElevatedVariant,
                )
            }

            build.topErrors.forEach { issue ->
                Column {
                    Text(
                        text = TextResource.RawText(issue.message),
                        style = BuildNotifyTheme.typography.bodySmall,
                        color = BuildNotifyTheme.colors.status.error.main,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    issue.location?.let { location ->
                        Text(
                            text = TextResource.RawText(location),
                            style = BuildNotifyTheme.typography.code.small,
                            color = BuildNotifyTheme.colors.content.onElevatedVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            val hidden = build.errorCount - build.topErrors.size
            if (hidden > 0) {
                Text(
                    text = textResource(Res.string.build_more_errors, hidden),
                    style = BuildNotifyTheme.typography.labelSmall,
                    color = BuildNotifyTheme.colors.content.onElevatedVariant,
                )
            }
        }
    }
}
