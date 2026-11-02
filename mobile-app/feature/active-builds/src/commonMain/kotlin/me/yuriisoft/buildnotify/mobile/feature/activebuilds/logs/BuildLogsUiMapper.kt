package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.toUi

class BuildLogsUiMapper {

    fun map(
        build: BuildSnapshot?,
        logs: List<BuildLogEntry>,
        filter: LogFilter,
    ): BuildLogsUiState {
        if (build == null && logs.isEmpty()) return BuildLogsUiState.NotFound

        val origin = logs.firstOrNull()?.timestamp ?: 0L
        val finished = build as? BuildSnapshot.Finished

        return BuildLogsUiState.Loaded(
            projectName = build?.projectName,
            status = build.toStatusUi(),
            filter = filter,
            lines = logs
                .filter { filter.accepts(it.kind) }
                .map { entry ->
                    BuildLogLineUi(
                        offset = formatOffset(entry.timestamp - origin),
                        text = entry.message,
                        kind = entry.kind,
                    )
                }
                .toImmutableList(),
            totalLines = logs.size,
            errors = finished?.outcome?.errors?.map { it.toUi() }?.toImmutableList()
                ?: persistentListOf(),
            warnings = finished?.outcome?.warnings?.map { it.toUi() }?.toImmutableList()
                ?: persistentListOf(),
        )
    }
}

private fun BuildSnapshot?.toStatusUi(): BuildStatusUi = when (this) {
    is BuildSnapshot.Active   -> BuildStatusUi.Running(startedAt, currentTask)
    is BuildSnapshot.Finished -> BuildStatusUi.Finished(outcome.status, outcome.durationMs)
    null                      -> BuildStatusUi.Unknown
}

/** `+0.0s`, `+12.4s`, `+3m 05s`. */
internal fun formatOffset(deltaMs: Long): String {
    val safe = deltaMs.coerceAtLeast(0)
    val totalSeconds = safe / 1_000
    if (totalSeconds < 60) {
        val tenths = (safe % 1_000) / 100
        return "+$totalSeconds.${tenths}s"
    }
    val minutes = totalSeconds / 60
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "+${minutes}m ${seconds}s"
}
