package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildResultEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotBuild
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskFinishedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BuildEventFoldTest {

    // region BuildStartedEvent

    @Test
    fun buildStarted_foldBuilds_addsActiveEntry() {
        val event = BuildStartedEvent(
            buildId = "b1",
            projectName = "app",
            startedAt = 1000L,
        )

        val result = event.foldBuilds(emptyMap())

        assertEquals(1, result.size)
        val snapshot = assertIs<BuildSnapshot.Active>(result["b1"])
        assertEquals("b1", snapshot.buildId)
        assertEquals("app", snapshot.projectName)
        assertEquals(1000L, snapshot.startedAt)
        assertEquals(null, snapshot.currentTask)
    }

    @Test
    fun buildStarted_foldBuilds_replacesExistingEntry() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "old",
                startedAt = 500L,
                currentTask = ":compile",
            ),
        )
        val event = BuildStartedEvent(
            buildId = "b1",
            projectName = "new-app",
            startedAt = 2000L,
        )

        val result = event.foldBuilds(existing)

        assertEquals(1, result.size)
        val snapshot = assertIs<BuildSnapshot.Active>(result["b1"])
        assertEquals("new-app", snapshot.projectName)
        assertEquals(2000L, snapshot.startedAt)
        assertEquals(null, snapshot.currentTask)
    }

    @Test
    fun buildStarted_foldLogs_returnsUnchanged() {
        val logs = mapOf(
            "b1" to listOf(BuildLogEntry(100L, "task output", LogKind.TASK)),
        )
        val event = BuildStartedEvent(
            buildId = "b1",
            projectName = "app",
            startedAt = 1000L,
        )

        assertEquals(logs, event.foldLogs(logs))
    }

    // endregion

    // region TaskStartedEvent

    @Test
    fun taskStarted_foldBuilds_updatesCurrentTask() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                currentTask = null,
            ),
        )
        val event = TaskStartedEvent(
            buildId = "b1",
            taskPath = ":app:compileKotlin",
            timestamp = 1100L,
        )

        val result = event.foldBuilds(existing)

        val snapshot = assertIs<BuildSnapshot.Active>(result["b1"])
        assertEquals(":app:compileKotlin", snapshot.currentTask)
        assertEquals("app", snapshot.projectName)
        assertEquals(1000L, snapshot.startedAt)
    }

    @Test
    fun taskStarted_foldBuilds_noopWhenBuildAbsent() {
        val existing = mapOf(
            "other" to BuildSnapshot.Active(
                buildId = "other",
                projectName = "lib",
                startedAt = 1000L,
                currentTask = null,
            ),
        )
        val event = TaskStartedEvent(
            buildId = "b1",
            taskPath = ":app:compileKotlin",
            timestamp = 1100L,
        )

        assertEquals(existing, event.foldBuilds(existing))
    }

    @Test
    fun taskStarted_foldLogs_appendsTaskEntry() {
        val event = TaskStartedEvent(
            buildId = "b1",
            taskPath = ":app:compileKotlin",
            timestamp = 1100L,
        )

        val result = event.foldLogs(emptyMap())

        val entries = result["b1"]!!
        assertEquals(1, entries.size)
        assertEquals(LogKind.TASK, entries[0].kind)
        assertEquals(1100L, entries[0].timestamp)
        assertTrue(entries[0].message.contains(":app:compileKotlin"))
    }

    @Test
    fun taskStarted_foldLogs_appendsToCorrectBuildId() {
        val existing = mapOf(
            "b1" to listOf(BuildLogEntry(1000L, "first task", LogKind.TASK)),
            "b2" to listOf(BuildLogEntry(1000L, "other build", LogKind.TASK)),
        )
        val event = TaskStartedEvent(
            buildId = "b1",
            taskPath = ":app:test",
            timestamp = 1200L,
        )

        val result = event.foldLogs(existing)

        assertEquals(2, result["b1"]!!.size)
        assertEquals(LogKind.TASK, result["b1"]!!.last().kind)
        assertEquals(1, result["b2"]!!.size, "other build's logs must not be affected")
    }

    // endregion

    // region TaskFinishedEvent

    @Test
    fun taskFinished_foldBuilds_returnsUnchanged() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                currentTask = ":app:compileKotlin",
            ),
        )
        val event = TaskFinishedEvent(
            buildId = "b1",
            taskPath = ":app:compileKotlin",
            status = "SUCCESS",
            timestamp = 1300L,
        )

        assertEquals(existing, event.foldBuilds(existing))
    }

    @Test
    fun taskFinished_foldLogs_appendsTaskEntryWithStatus() {
        val event = TaskFinishedEvent(
            buildId = "b1",
            taskPath = ":app:compileKotlin",
            status = "UP-TO-DATE",
            timestamp = 1300L,
        )

        val result = event.foldLogs(emptyMap())

        val entries = result["b1"]!!
        assertEquals(1, entries.size)
        assertEquals(LogKind.TASK, entries[0].kind)
        assertEquals(1300L, entries[0].timestamp)
        assertTrue(entries[0].message.contains(":app:compileKotlin"))
        assertTrue(entries[0].message.contains("UP-TO-DATE"))
    }

    // endregion

    // region DiagnosticEvent

    @Test
    fun diagnostic_foldBuilds_returnsUnchanged() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                currentTask = ":app:compileKotlin",
            ),
        )
        val event = DiagnosticEvent(
            buildId = "b1",
            severity = DiagnosticSeverity.WARNING,
            message = "deprecated API usage",
            filePath = "Foo.kt",
            line = 42,
            timestamp = 1200L,
        )

        assertEquals(existing, event.foldBuilds(existing))
    }

    @Test
    fun diagnostic_foldLogs_appendsWarningEntry() {
        val event = DiagnosticEvent(
            buildId = "b1",
            severity = DiagnosticSeverity.WARNING,
            message = "deprecated API usage",
            filePath = "Foo.kt",
            line = 42,
            timestamp = 1200L,
        )

        val result = event.foldLogs(emptyMap())

        val entries = result["b1"]!!
        assertEquals(1, entries.size)
        assertEquals(LogKind.WARNING, entries[0].kind)
        assertEquals(1200L, entries[0].timestamp)
        assertTrue(entries[0].message.contains("deprecated API usage"))
    }

    @Test
    fun diagnostic_foldLogs_appendsErrorEntry() {
        val event = DiagnosticEvent(
            buildId = "b1",
            severity = DiagnosticSeverity.ERROR,
            message = "unresolved reference: foo",
            filePath = "Bar.kt",
            line = 10,
            timestamp = 1300L,
        )

        val result = event.foldLogs(emptyMap())

        val entries = result["b1"]!!
        assertEquals(1, entries.size)
        assertEquals(LogKind.ERROR, entries[0].kind)
        assertEquals(1300L, entries[0].timestamp)
        assertTrue(entries[0].message.contains("unresolved reference: foo"))
    }

    // endregion

    // region BuildResultEvent

    @Test
    fun buildResult_foldBuilds_replacesActiveWithFinished() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                currentTask = ":app:test",
            ),
        )
        val event = BuildResultEvent(
            buildId = "b1",
            projectName = "app",
            status = FinishStatus.SUCCESS,
            durationMs = 5000L,
            startedAt = 1000L,
            finishedAt = 6000L,
            errors = emptyList(),
            warnings = emptyList(),
        )

        val result = event.foldBuilds(existing)

        val snapshot = assertIs<BuildSnapshot.Finished>(result["b1"])
        assertEquals("b1", snapshot.buildId)
        assertEquals("app", snapshot.projectName)
        assertEquals(1000L, snapshot.startedAt)
        assertEquals(FinishStatus.SUCCESS, snapshot.outcome.status)
        assertEquals(5000L, snapshot.outcome.durationMs)
        assertTrue(snapshot.outcome.errors.isEmpty())
        assertTrue(snapshot.outcome.warnings.isEmpty())
    }

    @Test
    fun buildResult_foldBuilds_carriesErrorsAndWarnings() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                currentTask = null,
            ),
        )
        val errors = listOf(BuildIssue("compilation error", "Foo.kt", 10))
        val warnings = listOf(
            BuildIssue("deprecated call", "Bar.kt", 5),
            BuildIssue("unused import", "Baz.kt", 1),
        )
        val event = BuildResultEvent(
            buildId = "b1",
            projectName = "app",
            status = FinishStatus.FAILED,
            durationMs = 3000L,
            startedAt = 1000L,
            finishedAt = 4000L,
            errors = errors,
            warnings = warnings,
        )

        val result = event.foldBuilds(existing)

        val snapshot = assertIs<BuildSnapshot.Finished>(result["b1"])
        assertEquals(FinishStatus.FAILED, snapshot.outcome.status)
        assertEquals(3000L, snapshot.outcome.durationMs)
        assertEquals(1, snapshot.outcome.errors.size)
        assertEquals("compilation error", snapshot.outcome.errors[0].message)
        assertEquals(2, snapshot.outcome.warnings.size)
    }

    @Test
    fun buildResult_foldBuilds_noopWhenNoActiveEntry() {
        val existing = mapOf(
            "other" to BuildSnapshot.Active(
                buildId = "other",
                projectName = "lib",
                startedAt = 500L,
                currentTask = null,
            ),
        )
        val event = BuildResultEvent(
            buildId = "b1",
            projectName = "app",
            status = FinishStatus.SUCCESS,
            durationMs = 5000L,
            startedAt = 1000L,
            finishedAt = 6000L,
            errors = emptyList(),
            warnings = emptyList(),
        )

        val result = event.foldBuilds(existing)

        assertEquals(existing, result, "result for unknown build must be ignored")
    }

    @Test
    fun buildResult_foldBuilds_noopWhenBuildAlreadyFinished() {
        val existing = mapOf(
            "b1" to BuildSnapshot.Finished(
                buildId = "b1",
                projectName = "app",
                startedAt = 1000L,
                outcome = BuildOutcome(
                    status = FinishStatus.FAILED,
                    durationMs = 2000L,
                    errors = emptyList(),
                    warnings = emptyList(),
                ),
            ),
        )
        val event = BuildResultEvent(
            buildId = "b1",
            projectName = "app",
            status = FinishStatus.SUCCESS,
            durationMs = 5000L,
            startedAt = 1000L,
            finishedAt = 6000L,
            errors = emptyList(),
            warnings = emptyList(),
        )

        val result = event.foldBuilds(existing)

        assertEquals(existing, result, "duplicate result must not overwrite existing Finished")
    }

    @Test
    fun buildResult_foldLogs_returnsUnchanged() {
        val logs = mapOf(
            "b1" to listOf(BuildLogEntry(1100L, ":app:compile", LogKind.TASK)),
        )
        val event = BuildResultEvent(
            buildId = "b1",
            projectName = "app",
            status = FinishStatus.SUCCESS,
            durationMs = 5000L,
            startedAt = 1000L,
            finishedAt = 6000L,
            errors = emptyList(),
            warnings = emptyList(),
        )

        assertEquals(logs, event.foldLogs(logs))
    }

    // endregion

    // region SnapshotEvent

    @Test
    fun snapshot_foldBuilds_replacesEntireMap() {
        val existing = mapOf(
            "old" to BuildSnapshot.Active(
                buildId = "old",
                projectName = "stale-app",
                startedAt = 100L,
                currentTask = null,
            ),
        )
        val event = SnapshotEvent(
            activeBuilds = listOf(
                SnapshotBuild(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1000L,
                    currentTask = ":compile",
                ),
            ),
            recentResults = emptyList(),
        )

        val result = event.foldBuilds(existing)

        assertEquals(1, result.size)
        assertTrue("old" !in result, "stale entry must be evicted")
        val snapshot = assertIs<BuildSnapshot.Active>(result["b1"])
        assertEquals("app", snapshot.projectName)
        assertEquals(1000L, snapshot.startedAt)
        assertEquals(":compile", snapshot.currentTask)
    }

    @Test
    fun snapshot_foldBuilds_includesRecentResults() {
        val event = SnapshotEvent(
            activeBuilds = listOf(
                SnapshotBuild(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1000L,
                    currentTask = null,
                ),
            ),
            recentResults = listOf(
                BuildResultEvent(
                    buildId = "b2",
                    projectName = "lib",
                    status = FinishStatus.FAILED,
                    durationMs = 2000L,
                    startedAt = 500L,
                    finishedAt = 2500L,
                    errors = listOf(BuildIssue("link error", null, null)),
                    warnings = emptyList(),
                ),
            ),
        )

        val result = event.foldBuilds(emptyMap())

        assertEquals(2, result.size)
        assertIs<BuildSnapshot.Active>(result["b1"])
        val finished = assertIs<BuildSnapshot.Finished>(result["b2"])
        assertEquals("lib", finished.projectName)
        assertEquals(FinishStatus.FAILED, finished.outcome.status)
        assertEquals(2000L, finished.outcome.durationMs)
        assertEquals(1, finished.outcome.errors.size)
    }

    @Test
    fun snapshot_foldLogs_clearsAllLogs() {
        val existing = mapOf(
            "b1" to listOf(BuildLogEntry(100L, "task output", LogKind.TASK)),
            "b2" to listOf(BuildLogEntry(200L, "warning msg", LogKind.WARNING)),
        )
        val event = SnapshotEvent(
            activeBuilds = emptyList(),
            recentResults = emptyList(),
        )

        val result = event.foldLogs(existing)

        assertTrue(result.isEmpty(), "all logs must be cleared on snapshot")
    }

    // endregion
}
