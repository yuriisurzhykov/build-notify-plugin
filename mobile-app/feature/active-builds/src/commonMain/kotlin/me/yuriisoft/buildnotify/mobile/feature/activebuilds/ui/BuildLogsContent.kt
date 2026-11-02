package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.FilterChip
import me.yuriisoft.buildnotify.mobile.designsystem.components.IconButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteTextStyle
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_back
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_unknown_project
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_copy
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_all
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_problems
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filtered_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_jump_to_end
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_output
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_warnings
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogLineUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildStatusUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.LogFilter
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.BuildIssueUi
import org.jetbrains.compose.resources.StringResource
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
        TopBar(state, onBack, onCopied)
        when (state) {
            BuildLogsUiState.Loading   -> Centered { BouncingDots(color = GraphiteTheme.colors.running.main) }
            BuildLogsUiState.NotFound  -> Centered { NotFound() }
            is BuildLogsUiState.Loaded -> LoadedLogs(state, onIntent)
        }
    }
}

@Composable
private fun TopBar(state: BuildLogsUiState, onBack: () -> Unit, onCopied: () -> Unit) {
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
            is BuildStatusUi.Running  -> StatusPill(BuildTone.Running, stringResource((null as FinishStatus?).label()))
            is BuildStatusUi.Finished -> StatusPill(status.status.tone(), stringResource(status.status.label()))
            else                      -> Unit
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
        BuildStatusUi.Unknown     -> stringResource(Res.string.build_unknown_project)
    }
    Text(text, style = GraphiteTheme.typography.mono, color = GraphiteTheme.colors.contentSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadedLogs(
    state: BuildLogsUiState.Loaded,
    onIntent: (BuildLogsIntent) -> Unit,
) {
    val spacing = GraphiteTheme.spacing
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val atBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= info.totalItemsCount - 2
        }
    }
    // Follow the tail like `tail -f`, but only while the reader is already at the bottom.
    LaunchedEffect(state.lines.size) {
        val follow = atBottom
        withFrameNanos { }
        val total = listState.layoutInfo.totalItemsCount
        if (follow && total > 0) listState.animateScrollToItem(total - 1)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.l, vertical = spacing.s),
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        LogFilter.entries.forEach { filter ->
            FilterChip(
                text = stringResource(filter.label()),
                selected = filter == state.filter,
                onClick = { onIntent(BuildLogsIntent.SelectFilter(filter)) },
                count = state.filterCounts[filter],
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.l, end = spacing.l, bottom = 96.dp),
        ) {
            if (state.filter.accepts(LogKind.ERROR)) {
                issueSection(Res.string.logs_section_errors, state.errors, BuildTone.Failure)
            }
            if (state.filter.accepts(LogKind.WARNING)) {
                issueSection(Res.string.logs_section_warnings, state.warnings, BuildTone.Warning)
            }
            item(key = "output_header") {
                SectionHeader(
                    stringResource(Res.string.logs_section_output),
                    Modifier.animateItem().padding(top = spacing.l),
                )
            }
            if (state.lines.isEmpty()) {
                item(key = "output_empty") {
                    Text(
                        stringResource(if (state.totalLines == 0) Res.string.logs_empty else Res.string.logs_filtered_empty),
                        modifier = Modifier.animateItem(),
                        style = GraphiteTheme.typography.bodySmall,
                        color = GraphiteTheme.colors.contentTertiary,
                    )
                }
            } else {
                consoleLines(state.lines)
            }
        }

        AnimatedVisibility(
            visible = !atBottom,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(spacing.xl),
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
        ) {
            Button(
                text = stringResource(Res.string.logs_jump_to_end),
                onClick = {
                    scope.launch {
                        val total = listState.layoutInfo.totalItemsCount
                        if (total > 0) listState.animateScrollToItem(total - 1)
                    }
                },
                variant = ButtonVariant.Secondary,
                leadingIcon = GraphiteIcons.ArrowDown,
            )
        }
    }
}

/**
 * Log lines as one continuous console: each line is its own lazy item (thousands of lines
 * stay cheap), with the console's rounded corners only on the first and last line.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.consoleLines(lines: ImmutableList<BuildLogLineUi>) {
    itemsIndexed(lines, key = { index, _ -> "line_$index" }, contentType = { _, _ -> "line" }) { index, line ->
        val radius = 16.dp
        val shape = RoundedCornerShape(
            topStart = if (index == 0) radius else 0.dp,
            topEnd = if (index == 0) radius else 0.dp,
            bottomStart = if (index == lines.lastIndex) radius else 0.dp,
            bottomEnd = if (index == lines.lastIndex) radius else 0.dp,
        )
        Box(
            Modifier
                .animateItem()
                .fillMaxWidth()
                .background(GraphiteTheme.colors.console, shape)
                .padding(
                    horizontal = GraphiteTheme.spacing.s,
                    vertical = if (index == 0 || index == lines.lastIndex) 6.dp else 0.dp,
                ),
        ) {
            CompositionLocalProvider(LocalGraphiteTextStyle provides GraphiteTheme.typography.log) {
                LogLine(line.text, tone = line.kind.tone(), offset = line.offset)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.issueSection(
    title: StringResource,
    issues: ImmutableList<BuildIssueUi>,
    tone: BuildTone,
) {
    if (issues.isEmpty()) return
    item(key = "issues_header_$tone") {
        Row(
            modifier = Modifier.animateItem().padding(top = GraphiteTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(stringResource(title))
            StatusPill(tone, issues.size.toString(), Modifier.padding(bottom = 6.dp))
        }
    }
    itemsIndexed(issues, key = { index, _ -> "issue_${tone}_$index" }) { index, issue ->
        val colors = GraphiteTheme.colors.tone(tone)
        Reveal(index, Modifier.animateItem().padding(bottom = GraphiteTheme.spacing.s)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.container, GraphiteTheme.shapes.control)
                    .padding(GraphiteTheme.spacing.m),
            ) {
                IssueText(issue.message, issue.location, locationColor = colors.main)
            }
        }
    }
}

private fun LogFilter.label(): StringResource = when (this) {
    LogFilter.ALL      -> Res.string.logs_filter_all
    LogFilter.PROBLEMS -> Res.string.logs_filter_problems
    LogFilter.ERRORS   -> Res.string.logs_filter_errors
}

@Composable
private fun NotFound() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        modifier = Modifier.padding(GraphiteTheme.spacing.xxl),
    ) {
        Text(stringResource(Res.string.logs_not_found_title), style = GraphiteTheme.typography.headline, textAlign = TextAlign.Center)
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
