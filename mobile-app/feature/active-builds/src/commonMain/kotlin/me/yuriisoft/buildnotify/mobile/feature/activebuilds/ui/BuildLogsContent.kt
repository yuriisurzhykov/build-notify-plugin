package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_back
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_unknown_project
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_all
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_problems
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filtered_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_not_found_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_output
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_warnings
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildStatusUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.LogFilter
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.BuildIssueUi
import me.yuriisoft.buildnotify.mobile.ui.components.button.IconButton
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Badge
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.components.layout.CodeSurface
import me.yuriisoft.buildnotify.mobile.ui.components.layout.Surface
import me.yuriisoft.buildnotify.mobile.ui.components.progress.CircularProgress
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.circular.CircularProgressMode
import me.yuriisoft.buildnotify.mobile.ui.resource.ImageResource
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import me.yuriisoft.buildnotify.mobile.ui.resource.textResource
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme
import org.jetbrains.compose.resources.StringResource

@Composable
internal fun BuildLogsContent(
    state: BuildLogsUiState,
    onIntent: (BuildLogsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        TopBar(state = state, onBack = onBack)
        when (state) {
            BuildLogsUiState.Loading   -> Centered {
                CircularProgress(mode = CircularProgressMode.Indeterminate())
            }

            BuildLogsUiState.NotFound  -> Centered { NotFound() }
            is BuildLogsUiState.Loaded -> LoadedLogs(state, onIntent)
        }
    }
}

@Composable
private fun TopBar(state: BuildLogsUiState, onBack: () -> Unit) {
    val spacing = BuildNotifyTheme.dimensions.spacing
    val loaded = state as? BuildLogsUiState.Loaded

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.small, vertical = spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            image = ImageResource.VectorImage(BackIcon),
            onClick = onBack,
            contentDescription = textResource(Res.string.action_back),
            contentColor = BuildNotifyTheme.colors.content.primary,
        )
        Spacer(Modifier.width(spacing.small))
        Column(Modifier.weight(1f)) {
            Text(
                text = loaded?.projectName?.let { TextResource.RawText(it) }
                    ?: textResource(Res.string.logs_title),
                style = BuildNotifyTheme.typography.titleLarge,
                color = BuildNotifyTheme.colors.content.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            loaded?.let { StatusLine(it.status) }
        }
        loaded?.status?.let { status ->
            when (status) {
                is BuildStatusUi.Running  -> BuildStatusBadge(status = null)
                is BuildStatusUi.Finished -> BuildStatusBadge(status = status.status)
                BuildStatusUi.Unknown     -> Unit
            }
        }
        Spacer(Modifier.width(spacing.small))
    }
}

@Composable
private fun StatusLine(status: BuildStatusUi) {
    val text: TextResource = when (status) {
        is BuildStatusUi.Running  -> {
            val elapsed = formatDuration(rememberElapsedMs(status.startedAt))
            val task = status.currentTask
            if (task != null) TextResource.RawText("$elapsed · $task")
            else TextResource.RawText(elapsed)
        }

        is BuildStatusUi.Finished -> TextResource.RawText(formatDuration(status.durationMs))
        BuildStatusUi.Unknown     -> textResource(Res.string.build_unknown_project)
    }
    Text(
        text = text,
        style = BuildNotifyTheme.typography.bodySmall,
        color = BuildNotifyTheme.colors.content.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun LoadedLogs(
    state: BuildLogsUiState.Loaded,
    onIntent: (BuildLogsIntent) -> Unit,
) {
    val spacing = BuildNotifyTheme.dimensions.spacing
    val padding = BuildNotifyTheme.dimensions.layout.contentPadding
    val listState = rememberLazyListState()

    // Follow the tail like `tail -f`, but only while the user is already at the bottom.
    val atBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= info.totalItemsCount - 2
        }
    }
    LaunchedEffect(state.lines.size) {
        val follow = atBottom
        withFrameNanos { } // let the list lay out the new lines first
        val total = listState.layoutInfo.totalItemsCount
        if (follow && total > 0) listState.animateScrollToItem(total - 1)
    }

    FilterRow(selected = state.filter, onSelect = { onIntent(BuildLogsIntent.SelectFilter(it)) })

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = padding, end = padding, bottom = spacing.xxLarge),
        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        if (state.filter.accepts(LogKind.ERROR)) {
            issueSection(Res.string.logs_section_errors, state.errors, isError = true)
        }
        if (state.filter != LogFilter.ERRORS) {
            issueSection(Res.string.logs_section_warnings, state.warnings, isError = false)
        }

        item(key = "output_header", contentType = "header") {
            SectionTitle(Res.string.logs_section_output)
        }
        if (state.lines.isEmpty()) {
            item(key = "output_empty", contentType = "empty") {
                Text(
                    text = textResource(
                        if (state.totalLines == 0) Res.string.logs_empty
                        else Res.string.logs_filtered_empty,
                    ),
                    style = BuildNotifyTheme.typography.bodySmall,
                    color = BuildNotifyTheme.colors.content.tertiary,
                )
            }
        } else {
            itemsIndexed(state.lines, key = { index, _ -> "line_$index" }, contentType = { _, _ -> "line" }) { _, line ->
                CodeSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = spacing.small, vertical = spacing.tiny)) {
                        Text(
                            text = TextResource.RawText(line.offset),
                            style = BuildNotifyTheme.typography.code.small,
                            color = BuildNotifyTheme.colors.content.onCodeVariant,
                            modifier = Modifier.width(64.dp),
                        )
                        LogLine(text = line.text, kind = line.kind, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun LazyListScope.issueSection(
    title: StringResource,
    issues: ImmutableList<BuildIssueUi>,
    isError: Boolean,
) {
    if (issues.isEmpty()) return
    item(key = "issues_header_$isError", contentType = "header") { SectionTitle(title, issues.size) }
    itemsIndexed(issues, key = { index, _ -> "issue_${isError}_$index" }, contentType = { _, _ -> "issue" }) { _, issue ->
        IssueRow(issue = issue, isError = isError)
    }
}

@Composable
private fun SectionTitle(title: StringResource, count: Int? = null) {
    Row(
        modifier = Modifier.padding(top = BuildNotifyTheme.dimensions.spacing.regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = textResource(title),
            style = BuildNotifyTheme.typography.labelLarge,
            color = BuildNotifyTheme.colors.content.secondary,
        )
        if (count != null) {
            Spacer(Modifier.width(BuildNotifyTheme.dimensions.spacing.tiny))
            Badge(containerColor = BuildNotifyTheme.colors.surface.elevated) {
                Text(
                    text = TextResource.RawText(count.toString()),
                    style = BuildNotifyTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun IssueRow(issue: BuildIssueUi, isError: Boolean) {
    val role = if (isError) BuildNotifyTheme.colors.status.error else BuildNotifyTheme.colors.status.warning
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = role.container,
        contentColor = role.onContainer,
        shape = BuildNotifyTheme.shapes.small,
    ) {
        Column(Modifier.padding(BuildNotifyTheme.dimensions.spacing.small)) {
            Text(
                text = TextResource.RawText(issue.message),
                style = BuildNotifyTheme.typography.bodySmall,
            )
            issue.location?.let {
                Text(
                    text = TextResource.RawText(it),
                    style = BuildNotifyTheme.typography.code.small,
                )
            }
        }
    }
}

@Composable
private fun FilterRow(selected: LogFilter, onSelect: (LogFilter) -> Unit) {
    val spacing = BuildNotifyTheme.dimensions.spacing
    Row(
        modifier = Modifier.padding(
            horizontal = BuildNotifyTheme.dimensions.layout.contentPadding,
            vertical = spacing.tiny,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        LogFilter.entries.forEach { filter ->
            FilterChip(
                label = filter.label(),
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

private fun LogFilter.label(): StringResource = when (this) {
    LogFilter.ALL      -> Res.string.logs_filter_all
    LogFilter.PROBLEMS -> Res.string.logs_filter_problems
    LogFilter.ERRORS   -> Res.string.logs_filter_errors
}

@Composable
private fun FilterChip(label: StringResource, selected: Boolean, onClick: () -> Unit) {
    val colors = BuildNotifyTheme.colors
    Surface(
        onClick = onClick,
        color = if (selected) colors.primary.container else Color.Transparent,
        contentColor = if (selected) colors.primary.onContainer else colors.content.secondary,
        shape = BuildNotifyTheme.shapes.full,
        border = if (selected) null else BorderStroke(
            BuildNotifyTheme.dimensions.stroke.thin,
            colors.outline,
        ),
    ) {
        Text(
            text = textResource(label),
            style = BuildNotifyTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = BuildNotifyTheme.dimensions.spacing.regular,
                vertical = BuildNotifyTheme.dimensions.spacing.tiny,
            ),
        )
    }
}

@Composable
private fun NotFound() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(BuildNotifyTheme.dimensions.layout.contentPadding),
    ) {
        Text(
            text = textResource(Res.string.logs_not_found_title),
            style = BuildNotifyTheme.typography.titleMedium,
            color = BuildNotifyTheme.colors.content.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = textResource(Res.string.logs_not_found_body),
            style = BuildNotifyTheme.typography.bodyMedium,
            color = BuildNotifyTheme.colors.content.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

private val BackIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Back",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            lineTo(7.83f, 11f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            lineTo(20f, 13f)
            close()
        }
    }.build()
}
