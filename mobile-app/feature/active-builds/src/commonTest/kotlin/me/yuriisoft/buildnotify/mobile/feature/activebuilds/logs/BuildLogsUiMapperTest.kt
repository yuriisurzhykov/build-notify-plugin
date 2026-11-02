package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BuildLogsUiMapperTest {

    private val mapper = BuildLogsUiMapper()

    private val logs = listOf(
        BuildLogEntry(1_000, ":app:compileKotlin", LogKind.TASK),
        BuildLogEntry(2_500, "Deprecated API", LogKind.WARNING),
        BuildLogEntry(4_000, "Unresolved reference", LogKind.ERROR),
    )

    @Test
    fun unknownBuildWithoutLogs_isNotFound() {
        assertEquals(BuildLogsUiState.NotFound, mapper.map(null, emptyList(), LogFilter.ALL))
    }

    @Test
    fun logsWithoutBuildRow_areStillShown() {
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, logs, LogFilter.ALL))
        assertEquals(BuildStatusUi.Unknown, state.status)
        assertEquals(3, state.lines.size)
    }

    @Test
    fun filters_narrowLinesButKeepTotal() {
        val build = BuildSnapshot.Active("b", "app", startedAt = 0, currentTask = null)

        val problems = assertIs<BuildLogsUiState.Loaded>(mapper.map(build, logs, LogFilter.PROBLEMS))
        val errors = assertIs<BuildLogsUiState.Loaded>(mapper.map(build, logs, LogFilter.ERRORS))

        assertEquals(listOf(LogKind.WARNING, LogKind.ERROR), problems.lines.map { it.kind })
        assertEquals(listOf("Unresolved reference"), errors.lines.map { it.text })
        assertEquals(3, errors.totalLines)
    }

    @Test
    fun offsets_areRelativeToTheFirstLine() {
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, logs, LogFilter.ALL))
        assertEquals(listOf("+0.0s", "+1.5s", "+3.0s"), state.lines.map { it.offset })
    }

    @Test
    fun formatOffset_switchesToMinutes() {
        assertEquals("+59.9s", formatOffset(59_900))
        assertEquals("+3m 05s", formatOffset(185_000))
        assertEquals("+0.0s", formatOffset(-5))
    }
}
