package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import kotlinx.collections.immutable.toImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

/**
 * Pure mapping from domain snapshots (+ logs of running builds) to [ActiveBuildsUiState].
 * Running builds come first; both groups are ordered newest first.
 */
class ActiveBuildsUiMapper(
    private val recentLogLines: Int = RECENT_LOG_LINES,
    private val topErrors: Int = TOP_ERRORS,
) {

    fun map(
        snapshots: List<BuildSnapshot>,
        logs: Map<String, List<BuildLogEntry>>,
    ): ActiveBuildsUiState {
        if (snapshots.isEmpty()) return ActiveBuildsUiState.Empty

        val running = snapshots
            .filterIsInstance<BuildSnapshot.Active>()
            .sortedByDescending { it.startedAt }
            .map { it.toUi(logs[it.buildId].orEmpty()) }

        val finished = snapshots
            .filterIsInstance<BuildSnapshot.Finished>()
            .sortedByDescending { it.startedAt }
            .map { it.toUi() }

        return ActiveBuildsUiState.Loaded(
            running = running.toImmutableList(),
            finished = finished.toImmutableList(),
        )
    }

    private fun BuildSnapshot.Active.toUi(logs: List<BuildLogEntry>) = RunningBuildUi(
        buildId = buildId,
        projectName = projectName,
        startedAt = startedAt,
        currentTask = currentTask,
        recentLogs = logs.takeLast(recentLogLines)
            .map { LogLineUi(text = it.message, kind = it.kind) }
            .toImmutableList(),
    )

    private fun BuildSnapshot.Finished.toUi() = FinishedBuildUi(
        buildId = buildId,
        projectName = projectName,
        status = outcome.status,
        durationMs = outcome.durationMs,
        errorCount = outcome.errors.size,
        warningCount = outcome.warnings.size,
        topErrors = outcome.errors.take(topErrors).map { it.toUi() }.toImmutableList(),
    )

    private companion object {
        const val RECENT_LOG_LINES = 4
        const val TOP_ERRORS = 3
    }
}

internal fun BuildIssue.toUi(): BuildIssueUi = BuildIssueUi(
    message = message,
    location = filePath?.let { path -> line?.let { "$path:$it" } ?: path },
)
