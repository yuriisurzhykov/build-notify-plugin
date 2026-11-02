package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ActiveBuildsUiMapperTest {

    private val mapper = ActiveBuildsUiMapper(recentLogLines = 2, topErrors = 1)

    @Test
    fun noSnapshots_mapsToEmpty() {
        assertEquals(ActiveBuildsUiState.Empty, mapper.map(emptyList(), emptyMap()))
    }

    @Test
    fun runningAndFinished_areSplitAndSortedNewestFirst() {
        val state = mapper.map(
            snapshots = listOf(
                finished("old", startedAt = 1),
                active("r1", startedAt = 5),
                finished("new", startedAt = 9),
                active("r2", startedAt = 7),
            ),
            logs = emptyMap(),
        )

        val loaded = assertIs<ActiveBuildsUiState.Loaded>(state)
        assertEquals(listOf("r2", "r1"), loaded.running.map { it.buildId })
        assertEquals(listOf("new", "old"), loaded.finished.map { it.buildId })
    }

    @Test
    fun runningBuild_keepsOnlyTheLogTail() {
        val logs = (1..5).map { BuildLogEntry(it.toLong(), "line $it", LogKind.TASK) }

        val loaded = assertIs<ActiveBuildsUiState.Loaded>(
            mapper.map(listOf(active("r1", startedAt = 1)), mapOf("r1" to logs)),
        )

        assertEquals(listOf("line 4", "line 5"), loaded.running.single().recentLogs.map { it.text })
    }

    @Test
    fun finishedBuild_countsIssuesAndFormatsLocations() {
        val errors = listOf(
            BuildIssue("Unresolved reference", "src/Main.kt", 42),
            BuildIssue("Project-level", null, null),
        )
        val loaded = assertIs<ActiveBuildsUiState.Loaded>(
            mapper.map(listOf(finished("f", startedAt = 1, errors = errors)), emptyMap()),
        )

        val build = loaded.finished.single()
        assertEquals(2, build.errorCount)
        assertEquals(listOf(BuildIssueUi("Unresolved reference", "src/Main.kt:42")), build.topErrors)
    }

    private fun active(id: String, startedAt: Long) =
        BuildSnapshot.Active(id, "app", startedAt, currentTask = null)

    private fun finished(id: String, startedAt: Long, errors: List<BuildIssue> = emptyList()) =
        BuildSnapshot.Finished(
            buildId = id,
            projectName = "app",
            startedAt = startedAt,
            outcome = BuildOutcome(FinishStatus.FAILED, 1_000, errors, emptyList()),
        )
}
