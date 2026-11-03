package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.designsystem.components.ConsoleSegment
import me.yuriisoft.buildnotify.mobile.designsystem.components.ConsoleSegmentPosition
import me.yuriisoft.buildnotify.mobile.designsystem.components.IssueCard
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filtered_empty
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_section_output
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogLineUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.BuildIssueUi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Log lines as one continuous console: each line is its own lazy item (thousands of lines
 * stay cheap), with the console's rounded corners only on the first and last line.
 */
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.consoleLines(lines: ImmutableList<BuildLogLineUi>) {
    itemsIndexed(lines, key = { index, _ -> "line_$index" }, contentType = { _, _ -> "line" }) { index, line ->
        ConsoleSegment(
            position = ConsoleSegmentPosition.of(index, lines.size),
            modifier = Modifier.animateItem(),
        ) {
            LogLine(line.text, tone = line.kind.tone(), offset = line.offset)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.issueSection(
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
            StatusPill(tone, issues.size.toString(), Modifier.padding(bottom = IssueCountGap))
        }
    }
    itemsIndexed(issues, key = { index, _ -> "issue_${tone}_$index" }) { index, issue ->
        Reveal(index, Modifier.animateItem().padding(bottom = GraphiteTheme.spacing.s)) {
            IssueCard(issue.message, issue.location, tone)
        }
    }
}

/** "Log" header, then the lines — or a hint when nothing is logged or the filter hides all. */
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.outputSection(state: BuildLogsUiState.Loaded) {
    item(key = "output_header") {
        SectionHeader(
            stringResource(Res.string.logs_section_output),
            Modifier.animateItem().padding(top = GraphiteTheme.spacing.l),
        )
    }
    if (state.lines.isNotEmpty()) {
        consoleLines(state.lines)
        return
    }
    item(key = "output_empty") {
        Text(
            stringResource(if (state.totalLines == 0) Res.string.logs_empty else Res.string.logs_filtered_empty),
            modifier = Modifier.animateItem(),
            style = GraphiteTheme.typography.bodySmall,
            color = GraphiteTheme.colors.contentTertiary,
        )
    }
}

private val IssueCountGap = 6.dp
