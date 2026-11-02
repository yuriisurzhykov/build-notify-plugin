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
    ) : ActiveBuildsUiState
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
