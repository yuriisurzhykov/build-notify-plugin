package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.ConsoleSegment
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.ConsoleSegmentPosition
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.console.IssueCard
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
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
    // Keyed by the line's id, not its index: loading older lines prepends items, and
    // index keys would make every visible line look new (and the list jump).
    itemsIndexed(lines, key = { _, line -> "line_${line.id}" }, contentType = { _, _ -> "line" }) { index, line ->
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
            StatusPill(tone, issues.size.toString(), Modifier.padding(bottom = GraphiteTheme.spacing.inline))
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
internal fun LazyListScope.outputSection(state: BuildLogsUiState.Loaded, onLoadOlder: () -> Unit) {
    item(key = "output_header") {
        SectionHeader(
            stringResource(Res.string.logs_section_output),
            Modifier.animateItem().padding(top = GraphiteTheme.spacing.l),
        )
    }
    if (state.hasOlder) olderLinesTrigger(state.lines.firstOrNull()?.id, onLoadOlder)
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

/**
 * Sits above the oldest loaded line. A lazy list composes it only once it scrolls into
 * view, so composing it is the signal to load the page before; [oldestId] restarts the
 * request after each page arrives, in case the new page is still shorter than the screen.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.olderLinesTrigger(oldestId: Long?, onLoadOlder: () -> Unit) {
    item(key = "output_older", contentType = "older") {
        LaunchedEffect(oldestId) { onLoadOlder() }
        Box(Modifier.fillMaxWidth().animateItem().padding(vertical = GraphiteTheme.spacing.s)) {
            BouncingDots(Modifier.align(Alignment.Center))
        }
    }
}
