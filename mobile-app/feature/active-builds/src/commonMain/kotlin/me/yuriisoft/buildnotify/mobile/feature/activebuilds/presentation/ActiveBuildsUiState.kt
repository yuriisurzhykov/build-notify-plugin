package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind

@Immutable
sealed interface ActiveBuildsUiState {

    /** Before the first emission of the local cache. */
    @Immutable
    data object Loading : ActiveBuildsUiState

    /** Neither running nor recently finished builds are known. */
    @Immutable
    data object Empty : ActiveBuildsUiState

    /**
     * [streamFailed] keeps the last known lists visible with an inline banner instead
     * of replacing them with an error screen.
     */
    @Immutable
    data class Loaded(
        val running: ImmutableList<RunningBuildUi>,
        val finished: ImmutableList<FinishedBuildUi>,
        val streamFailed: Boolean = false,
        /** Open "hand off to an agent" sheet, or `null` when closed. */
        val handoff: HandoffUi? = null,
    ) : ActiveBuildsUiState
}

/** Content of the "hand off to an agent" sheet for one failed build. */
@Immutable
data class HandoffUi(
    val buildId: String,
    val projectName: String,
    val errorCount: Int,
    /** Ready-to-send message: errors with locations, optionally the log. */
    val prompt: String,
    val includeLog: Boolean,
    val logLineCount: Int,
)

/** Connection shown in the home top bar. */
@Immutable
sealed interface ConnectionUi {
    @Immutable
    data class Connected(val hostName: String) : ConnectionUi

    @Immutable
    data object Reconnecting : ConnectionUi

    @Immutable
    data object Offline : ConnectionUi
}

@Immutable
data class RunningBuildUi(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val currentTask: String?,
    val recentLogs: ImmutableList<LogLineUi>,
)

@Immutable
data class FinishedBuildUi(
    val buildId: String,
    val projectName: String,
    val status: FinishStatus,
    val startedAt: Long,
    val durationMs: Long,
    val errorCount: Int,
    val warningCount: Int,
    val topErrors: ImmutableList<BuildIssueUi>,
)

@Immutable
data class LogLineUi(
    val text: String,
    val kind: LogKind,
)

@Immutable
data class BuildIssueUi(
    val message: String,
    /** `path/File.kt:42`, or `null` for project-level diagnostics. */
    val location: String?,
)
