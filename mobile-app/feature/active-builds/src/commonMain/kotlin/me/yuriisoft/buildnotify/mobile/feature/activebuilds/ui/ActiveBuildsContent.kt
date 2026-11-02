package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.Card
import me.yuriisoft.buildnotify.mobile.designsystem.components.ConnectionBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.Console
import me.yuriisoft.buildnotify.mobile.designsystem.components.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.HistoryRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.InsetPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.ProgressBar
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.motion.GraphiteMotion
import me.yuriisoft.buildnotify.mobile.designsystem.motion.PopIn
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.motion.shakeOn
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_handoff
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_retry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_stop
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_issue_counts
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_preparing
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_section_recent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_stream_error
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_cancelled_label
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_cancelled_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_failed_label
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_failed_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_handoff_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_more_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_no_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_running_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_success_label
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_success_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.history_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.home_offline
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.home_reconnecting
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ConnectionUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.RunningBuildUi
import org.jetbrains.compose.resources.stringResource

/**
 * Home: one hero answering "is everything OK right now", then a dense history.
 *
 * Hero priority: the newest running build, else the newest finished one. Switching
 * between hero states morphs (fade + scale + size); new history rows slide in.
 */
@Composable
internal fun ActiveBuildsContent(
    state: ActiveBuildsUiState,
    connection: ConnectionUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        ScreenSurface(Modifier.fillMaxSize().safeDrawingPadding()) {
            TopBar(connection)
            when (state) {
                ActiveBuildsUiState.Loading   -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BouncingDots(color = GraphiteTheme.colors.running.main)
                }

                ActiveBuildsUiState.Empty     -> EmptyHero()
                is ActiveBuildsUiState.Loaded -> LoadedHome(state, onIntent)
            }
        }
        val handoff = (state as? ActiveBuildsUiState.Loaded)?.handoff
        HandoffOverlay(handoff = handoff, onIntent = onIntent, onCopied = onCopied)
    }
}

@Composable
private fun TopBar(connection: ConnectionUi) {
    val text = when (connection) {
        is ConnectionUi.Connected -> connection.hostName
        ConnectionUi.Reconnecting -> stringResource(Res.string.home_reconnecting)
        ConnectionUi.Offline      -> stringResource(Res.string.home_offline)
    }
    AnimatedContent(
        targetState = text to (connection is ConnectionUi.Connected),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.padding(horizontal = GraphiteTheme.spacing.screen, vertical = GraphiteTheme.spacing.l),
        label = "connection",
    ) { (label, connected) ->
        ConnectionBadge(text = label, connected = connected)
    }
}

// region Hero

@Immutable
private sealed interface Hero {
    val key: String

    @Immutable
    data class Running(val build: RunningBuildUi) : Hero {
        override val key: String = "running_${build.buildId}"
    }

    @Immutable
    data class Finished(val build: FinishedBuildUi) : Hero {
        override val key: String = "finished_${build.buildId}"
    }
}

/** History rows, everything except the build shown in the hero. */
@Immutable
private sealed interface HistoryItem {
    val key: String

    @Immutable
    data class Running(val build: RunningBuildUi) : HistoryItem {
        override val key: String = "h_running_${build.buildId}"
    }

    @Immutable
    data class Finished(val build: FinishedBuildUi) : HistoryItem {
        override val key: String = "h_finished_${build.buildId}"
    }
}

private fun ActiveBuildsUiState.Loaded.hero(): Hero? =
    running.firstOrNull()?.let { Hero.Running(it) } ?: finished.firstOrNull()?.let { Hero.Finished(it) }

private fun ActiveBuildsUiState.Loaded.history(hero: Hero?): List<HistoryItem> =
    running.filterNot { hero is Hero.Running && hero.build.buildId == it.buildId }.map { HistoryItem.Running(it) } +
        finished.filterNot { hero is Hero.Finished && hero.build.buildId == it.buildId }.map { HistoryItem.Finished(it) }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadedHome(
    state: ActiveBuildsUiState.Loaded,
    onIntent: (ActiveBuildsIntent) -> Unit,
) {
    val hero = state.hero()
    val history = state.history(hero)
    val spacing = GraphiteTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xxxl),
    ) {
        if (state.streamFailed) {
            item(key = "stream_error") {
                StreamErrorBanner(
                    onRetry = { onIntent(ActiveBuildsIntent.RetryStream) },
                    modifier = Modifier.animateItem().padding(horizontal = spacing.heroInset).padding(bottom = spacing.m),
                )
            }
        }
        item(key = "hero") {
            AnimatedContent(
                targetState = hero,
                contentKey = { it?.key },
                transitionSpec = {
                    (fadeIn(tween(GraphiteMotion.Medium)) + scaleIn(GraphiteMotion.enter(), initialScale = 0.96f))
                        .togetherWith(fadeOut(tween(GraphiteMotion.Fast)))
                        .using(SizeTransform(clip = false))
                },
                label = "hero",
            ) { current ->
                when (current) {
                    is Hero.Running  -> RunningHero(current.build, onIntent)
                    is Hero.Finished -> when (current.build.status) {
                        FinishStatus.FAILED    -> FailedHero(current.build, onIntent)
                        FinishStatus.SUCCESS   -> SuccessHero(current.build, onIntent)
                        FinishStatus.CANCELLED -> CancelledHero(current.build, onIntent)
                    }

                    null             -> EmptyHero()
                }
            }
        }
        if (history.isNotEmpty()) {
            item(key = "history_header") {
                SectionHeader(
                    stringResource(Res.string.builds_section_recent),
                    Modifier.animateItem().padding(start = spacing.screen, end = spacing.screen, top = spacing.section),
                )
            }
            items(history, key = { it.key }) { item ->
                Box(Modifier.animateItem().padding(horizontal = spacing.screen)) {
                    val isLast = item.key == history.last().key
                    when (item) {
                        is HistoryItem.Running  -> RunningHistoryRow(item.build, !isLast, onIntent)
                        is HistoryItem.Finished -> FinishedHistoryRow(item.build, !isLast, onIntent)
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningHero(build: RunningBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    val elapsed = rememberElapsedMs(build.startedAt)

    HeroPanel(BuildTone.Running) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusLabel(BuildTone.Running, stringResource(Res.string.hero_running))
            RollingText(formatTimer(elapsed), style = type.timer)
            Text(build.projectName, style = type.headline)
        }
        Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
            AnimatedContent(
                targetState = build.currentTask,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "currentTask",
            ) { task ->
                Text(
                    task ?: stringResource(Res.string.build_preparing),
                    style = type.mono,
                    color = colors.running.main,
                    maxLines = 1,
                )
            }
            ProgressBar(progress = null)
        }
        if (build.recentLogs.isNotEmpty()) {
            Console(translucent = true) {
                build.recentLogs.forEach { line ->
                    LogLine(line.text, tone = line.kind.tone(), singleLine = true)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
            DangerButton(
                text = stringResource(Res.string.action_stop),
                onClick = { onIntent(ActiveBuildsIntent.StopBuild(build.buildId)) },
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Stop,
            )
            SecondaryButton(
                text = stringResource(Res.string.action_logs),
                onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Terminal,
            )
        }
        Hint(stringResource(Res.string.hero_running_hint))
    }
}

@Composable
private fun FailedHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    val finishedAt = build.startedAt + build.durationMs

    HeroPanel(BuildTone.Failure) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusLabel(
                BuildTone.Failure,
                stringResource(Res.string.hero_failed_label, relativeTime(finishedAt)),
                Modifier.shakeOn(build.buildId),
            )
            Text(stringResource(Res.string.hero_failed_title, build.projectName), style = type.display)
            Text(formatDuration(build.durationMs), style = type.mono, color = colors.contentSecondary)
        }
        Reveal(1) {
            InsetPanel {
                val first = build.topErrors.firstOrNull()
                if (first == null) {
                    Text(stringResource(Res.string.hero_no_errors), style = type.bodySmall, color = colors.contentSecondary)
                } else {
                    IssueText(first.message, first.location)
                    val more = build.errorCount - 1
                    if (more > 0) {
                        Text(
                            stringResource(Res.string.hero_more_errors, more),
                            modifier = Modifier.padding(top = 6.dp),
                            style = type.meta,
                            color = colors.contentSecondary,
                        )
                    }
                }
            }
        }
        Reveal(2) {
            Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
                PrimaryButton(
                    text = stringResource(Res.string.action_handoff),
                    onClick = { onIntent(ActiveBuildsIntent.OpenHandoff(build.buildId)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = GraphiteIcons.ArrowRight,
                )
                Hint(stringResource(Res.string.hero_handoff_hint))
            }
        }
        Reveal(3) {
            Button(
                text = stringResource(Res.string.action_logs),
                onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Outline,
                leadingIcon = GraphiteIcons.Terminal,
            )
        }
    }
}

@Composable
private fun SuccessHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val finishedAt = build.startedAt + build.durationMs

    HeroPanel(BuildTone.Success) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PopIn { StatusBadge(BuildTone.Success) }
            Text(stringResource(Res.string.hero_success_title), style = type.display)
            Text(
                "${build.projectName} · ${formatDuration(build.durationMs)} · ${relativeTime(finishedAt)}",
                style = type.bodySmall,
                color = GraphiteTheme.colors.contentSecondary,
            )
            if (build.warningCount > 0) {
                Text(
                    stringResource(Res.string.build_issue_counts, 0, build.warningCount),
                    style = type.meta,
                    color = GraphiteTheme.colors.warning.main,
                )
            }
        }
        SecondaryButton(
            text = stringResource(Res.string.action_logs),
            onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Terminal,
        )
    }
}

@Composable
private fun CancelledHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val finishedAt = build.startedAt + build.durationMs
    HeroPanel(BuildTone.Warning) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusLabel(BuildTone.Warning, stringResource(Res.string.hero_cancelled_label, relativeTime(finishedAt)))
            Text(stringResource(Res.string.hero_cancelled_title, build.projectName), style = GraphiteTheme.typography.display)
            Text(formatDuration(build.durationMs), style = GraphiteTheme.typography.mono, color = GraphiteTheme.colors.contentSecondary)
        }
        SecondaryButton(
            text = stringResource(Res.string.action_logs),
            onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Terminal,
        )
    }
}

@Composable
private fun EmptyHero() {
    HeroPanel(BuildTone.Neutral) {
        Reveal(0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.builds_empty_title), style = GraphiteTheme.typography.display)
                Text(
                    stringResource(Res.string.builds_empty_body),
                    style = GraphiteTheme.typography.bodySmall,
                    color = GraphiteTheme.colors.contentSecondary,
                )
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        style = GraphiteTheme.typography.caption,
        color = GraphiteTheme.colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
}

// endregion

// region History

@Composable
private fun RunningHistoryRow(build: RunningBuildUi, showDivider: Boolean, onIntent: (ActiveBuildsIntent) -> Unit) {
    HistoryRow(
        tone = BuildTone.Running,
        statusDescription = stringResource((null as FinishStatus?).label()),
        title = build.projectName,
        subtitle = build.currentTask ?: stringResource(Res.string.history_running),
        duration = formatTimer(rememberElapsedMs(build.startedAt)),
        time = relativeTime(build.startedAt),
        showDivider = showDivider,
        onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
    )
}

@Composable
private fun FinishedHistoryRow(build: FinishedBuildUi, showDivider: Boolean, onIntent: (ActiveBuildsIntent) -> Unit) {
    val hasIssues = build.errorCount > 0 || build.warningCount > 0
    HistoryRow(
        tone = build.status.tone(),
        statusDescription = stringResource(build.status.label()),
        title = build.projectName,
        subtitle = if (hasIssues) {
            stringResource(Res.string.build_issue_counts, build.errorCount, build.warningCount)
        } else {
            stringResource(build.status.label())
        },
        duration = formatDuration(build.durationMs),
        time = relativeTime(build.startedAt + build.durationMs),
        showDivider = showDivider,
        onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
    )
}

@Composable
private fun StreamErrorBanner(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.builds_stream_error),
                modifier = Modifier.weight(1f),
                style = GraphiteTheme.typography.bodySmall,
                color = GraphiteTheme.colors.failure.main,
            )
            Button(
                text = stringResource(Res.string.action_retry),
                onClick = onRetry,
                variant = ButtonVariant.Outline,
                leadingIcon = GraphiteIcons.Rerun,
            )
        }
    }
}

// endregion
