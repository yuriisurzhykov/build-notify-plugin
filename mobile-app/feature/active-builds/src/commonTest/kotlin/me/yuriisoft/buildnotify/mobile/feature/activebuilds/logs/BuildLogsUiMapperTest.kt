package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BuildLogsUiMapperTest {

    private val mapper = BuildLogsUiMapper()

    private val logs = listOf(
        BuildLogEntry(1_000, ":app:compileKotlin", LogKind.TASK, id = 1),
        BuildLogEntry(2_500, "Deprecated API", LogKind.WARNING, id = 2),
        BuildLogEntry(4_000, "Unresolved reference", LogKind.ERROR, id = 3),
    )

    private val stats = LogStats(
        countsByKind = mapOf(LogKind.TASK to 1, LogKind.WARNING to 1, LogKind.ERROR to 1),
        firstTimestamp = 1_000,
    )

    @Test
    fun unknownBuildWithoutLogs_isNotFound() {
        assertEquals(BuildLogsUiState.NotFound, mapper.map(null, emptyList(), LogStats.Empty, LogFilter.ALL))
    }

    @Test
    fun logsWithoutBuildRow_areStillShown() {
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, logs, stats, LogFilter.ALL))
        assertEquals(BuildStatusUi.Unknown, state.status)
        assertEquals(3, state.lines.size)
    }

    @Test
    fun countsComeFromStats_notFromTheLoadedWindow() {
        val build = BuildSnapshot.Active("b", "app", startedAt = 0, currentTask = null)
        val big = LogStats(mapOf(LogKind.TASK to 5_000, LogKind.ERROR to 2), firstTimestamp = 0)

        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(build, logs.takeLast(1), big, LogFilter.ERRORS))

        assertEquals(5_002, state.totalLines)
        assertEquals(mapOf(LogFilter.ALL to 5_002, LogFilter.PROBLEMS to 2, LogFilter.ERRORS to 2), state.filterCounts)
        assertTrue(state.hasOlder, "1 of 2 error lines is loaded")
    }

    @Test
    fun fullyLoadedWindow_hasNoOlderLines() {
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, logs, stats, LogFilter.ALL))
        assertFalse(state.hasOlder)
    }

    @Test
    fun lines_keepTheirLogIds() {
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, logs, stats, LogFilter.ALL))
        assertEquals(listOf(1L, 2L, 3L), state.lines.map { it.id })
    }

    @Test
    fun offsets_areRelativeToTheFirstLineOfTheWholeLog() {
        val window = logs.drop(1)
        val state = assertIs<BuildLogsUiState.Loaded>(mapper.map(null, window, stats, LogFilter.ALL))
        assertEquals(listOf("+1.5s", "+3.0s"), state.lines.map { it.offset })
    }

    @Test
    fun formatOffset_switchesToMinutes() {
        assertEquals("+59.9s", formatOffset(59_900))
        assertEquals("+3m 05s", formatOffset(185_000))
        assertEquals("+0.0s", formatOffset(-5))
    }
}
