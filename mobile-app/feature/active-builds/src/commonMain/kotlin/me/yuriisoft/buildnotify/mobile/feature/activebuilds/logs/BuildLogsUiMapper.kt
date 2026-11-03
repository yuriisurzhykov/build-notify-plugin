package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogStats
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.TimeUnits
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.toUi

class BuildLogsUiMapper {

    /**
     * @param logs the loaded window, oldest first
     * @param stats the size of the whole log, of which [logs] is the newest part
     */
    fun map(
        build: BuildSnapshot?,
        logs: List<BuildLogEntry>,
        stats: LogStats,
        filter: LogFilter,
    ): BuildLogsUiState {
        if (build == null && stats.total == 0) return BuildLogsUiState.NotFound

        val origin = stats.firstTimestamp ?: logs.firstOrNull()?.timestamp ?: 0L
        val finished = build as? BuildSnapshot.Finished

        return BuildLogsUiState.Loaded(
            projectName = build?.projectName,
            status = build.toStatusUi(),
            filter = filter,
            lines = logs
                .map { entry ->
                    BuildLogLineUi(
                        id = entry.id,
                        offset = formatOffset(entry.timestamp - origin),
                        text = entry.message,
                        kind = entry.kind,
                    )
                }
                .toImmutableList(),
            hasOlder = logs.size < stats.count(filter.kinds),
            totalLines = stats.total,
            filterCounts = LogFilter.entries.associateWith { stats.count(it.kinds) },
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
    val totalSeconds = safe / TimeUnits.MILLIS_PER_SECOND
    if (totalSeconds < TimeUnits.SECONDS_PER_MINUTE) {
        val tenths = (safe % TimeUnits.MILLIS_PER_SECOND) / TimeUnits.MILLIS_PER_TENTH
        return "+$totalSeconds.${tenths}s"
    }
    val minutes = totalSeconds / TimeUnits.SECONDS_PER_MINUTE
    val seconds = (totalSeconds % TimeUnits.SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return "+${minutes}m ${seconds}s"
}
