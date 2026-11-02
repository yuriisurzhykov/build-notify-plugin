package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
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
        /** Lines that pass [filter]. */
        val lines: ImmutableList<BuildLogLineUi>,
        /** Number of lines before filtering — distinguishes "no logs" from "filtered out". */
        val totalLines: Int,
        /** How many lines each filter would show, for the chip counters. */
        val filterCounts: Map<LogFilter, Int>,
        val errors: ImmutableList<BuildIssueUi>,
        val warnings: ImmutableList<BuildIssueUi>,
    ) : BuildLogsUiState
}

@Immutable
sealed interface BuildStatusUi {

    @Immutable
    data class Running(val startedAt: Long, val currentTask: String?) : BuildStatusUi

    @Immutable
    data class Finished(val status: FinishStatus, val durationMs: Long) : BuildStatusUi

    /** Logs are cached but the build row is gone. */
    @Immutable
    data object Unknown : BuildStatusUi
}

@Immutable
data class BuildLogLineUi(
    /** Offset from the first line, e.g. `+12.4s`. */
    val offset: String,
    val text: String,
    val kind: LogKind,
)

enum class LogFilter(val accepts: (LogKind) -> Boolean) {
    ALL({ true }),
    PROBLEMS({ it == LogKind.WARNING || it == LogKind.ERROR }),
    ERRORS({ it == LogKind.ERROR }),
}

@Immutable
sealed interface BuildLogsIntent {

    @Immutable
    data class SelectFilter(val filter: LogFilter) : BuildLogsIntent
}
