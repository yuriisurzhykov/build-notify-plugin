package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.BuildIssueUi

@Immutable
sealed interface BuildLogsUiState {

    @Immutable
    data object Loading : BuildLogsUiState

    /** Neither the build nor any of its log lines are known (cache cleared, wrong id). */
    @Immutable
    data object NotFound : BuildLogsUiState

    @Immutable
    data class Loaded(
        val projectName: String?,
        val status: BuildStatusUi,
        val filter: LogFilter,
        /** The newest lines that pass [filter]; older ones are loaded on demand. */
        val lines: ImmutableList<BuildLogLineUi>,
        /** Whether lines passing [filter] exist before the first of [lines]. */
        val hasOlder: Boolean,
        /** Number of lines before filtering — distinguishes "no logs" from "filtered out". */
        val totalLines: Int,
        /** How many lines each filter would show, for the chip counters. */
        val filterCounts: Map<LogFilter, Int>,
        val errors: ImmutableList<BuildIssueUi>,
        val warnings: ImmutableList<BuildIssueUi>,
    ) : BuildLogsUiState
}
