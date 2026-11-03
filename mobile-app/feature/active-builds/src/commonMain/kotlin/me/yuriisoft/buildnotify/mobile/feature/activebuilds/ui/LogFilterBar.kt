package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.FilterChip
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_all
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_filter_problems
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.BuildLogsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs.LogFilter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Horizontally scrolling filter chips with per-filter line counts. */
@Composable
internal fun LogFilterBar(
    selected: LogFilter,
    counts: Map<LogFilter, Int>,
    onIntent: (BuildLogsIntent) -> Unit,
) {
    val spacing = GraphiteTheme.spacing
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
                selected = filter == selected,
                onClick = { onIntent(BuildLogsIntent.SelectFilter(filter)) },
                count = counts[filter],
            )
        }
    }
}

private fun LogFilter.label(): StringResource = when (this) {
    LogFilter.ALL      -> Res.string.logs_filter_all
    LogFilter.PROBLEMS -> Res.string.logs_filter_problems
    LogFilter.ERRORS   -> Res.string.logs_filter_errors
}
