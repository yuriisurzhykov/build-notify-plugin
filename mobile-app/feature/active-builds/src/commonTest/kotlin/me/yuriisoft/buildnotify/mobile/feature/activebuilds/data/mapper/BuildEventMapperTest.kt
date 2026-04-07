package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveBuildInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveTaskInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildDiagnosticPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildSnapshotPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStatus
import me.yuriisoft.buildnotify.mobile.data.protocol.CommandResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.CommandStatus
import me.yuriisoft.buildnotify.mobile.data.protocol.HeartbeatPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.HelloPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskFinishedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildResultEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskFinishedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildIssue as WireBuildIssue
import me.yuriisoft.buildnotify.mobile.data.protocol.DiagnosticSeverity as WireSeverity

class BuildEventMapperTest {

    private val fixedTime = 1_700_000_000_000L
    private val mapper = BuildEventMapper(clock = { fixedTime })

    // region BuildStartedPayload

    @Test
    fun buildStartedPayload_producesStartedEvent() {
        val payload = BuildStartedPayload(
            buildId = "b1",
            projectName = "app",
        )

        val result = mapper.map(payload)

        val event = assertIs<BuildStartedEvent>(result)
        assertEquals("b1", event.buildId)
        assertEquals("app", event.projectName)
        assertEquals(fixedTime, event.startedAt)
    }

    // endregion

    // region TaskStartedPayload

    @Test
    fun taskStartedPayload_producesTaskStartedEvent() {
        val payload = TaskStartedPayload(
            buildId = "b1",
            projectName = "app",
            taskPath = ":app:compileKotlin",
        )

        val result = mapper.map(payload)

        val event = assertIs<TaskStartedEvent>(result)
        assertEquals("b1", event.buildId)
        assertEquals(":app:compileKotlin", event.taskPath)
        assertEquals(fixedTime, event.timestamp)
    }

    // endregion

    // region TaskFinishedPayload

    @Test
    fun taskFinishedPayload_producesTaskFinishedEvent() {
        val payload = TaskFinishedPayload(
            buildId = "b1",
            projectName = "app",
            taskPath = ":app:compileKotlin",
            status = TaskStatus.SUCCESS,
        )

        val result = mapper.map(payload)

        val event = assertIs<TaskFinishedEvent>(result)
        assertEquals("b1", event.buildId)
        assertEquals(":app:compileKotlin", event.taskPath)
        assertEquals("SUCCESS", event.status)
        assertEquals(fixedTime, event.timestamp)
    }

    @Test
    fun taskFinishedPayload_preservesTaskStatusName() {
        val payload = TaskFinishedPayload(
            buildId = "b1",
            projectName = "app",
            taskPath = ":app:test",
            status = TaskStatus.UP_TO_DATE,
        )

        val event = assertIs<TaskFinishedEvent>(mapper.map(payload))

        assertEquals("UP_TO_DATE", event.status)
    }

    // endregion

    // region BuildDiagnosticPayload

    @Test
    fun diagnosticPayload_producesWarningEvent() {
        val payload = BuildDiagnosticPayload(
            buildId = "b1",
            projectName = "app",
            severity = WireSeverity.WARNING,
            message = "deprecated API usage",
            filePath = "Foo.kt",
            line = 42,
            column = 5,
        )

        val result = mapper.map(payload)

        val event = assertIs<DiagnosticEvent>(result)
        assertEquals("b1", event.buildId)
        assertEquals(DiagnosticSeverity.WARNING, event.severity)
        assertEquals("deprecated API usage", event.message)
        assertEquals("Foo.kt", event.filePath)
        assertEquals(42, event.line)
        assertEquals(fixedTime, event.timestamp)
    }

    @Test
    fun diagnosticPayload_producesErrorEvent() {
        val payload = BuildDiagnosticPayload(
            buildId = "b1",
            projectName = "app",
            severity = WireSeverity.ERROR,
            message = "unresolved reference: foo",
            filePath = "Bar.kt",
            line = 10,
        )

        val event = assertIs<DiagnosticEvent>(mapper.map(payload))

        assertEquals(DiagnosticSeverity.ERROR, event.severity)
    }

    @Test
    fun diagnosticPayload_handlesNullOptionalFields() {
        val payload = BuildDiagnosticPayload(
            buildId = "b1",
            projectName = "app",
            severity = WireSeverity.WARNING,
            message = "project-level warning",
        )

        val event = assertIs<DiagnosticEvent>(mapper.map(payload))

        assertNull(event.filePath)
        assertNull(event.line)
    }

    // endregion

    // region BuildResultPayload

    @Test
    fun buildResultPayload_producesResultEvent() {
        val payload = BuildResultPayload(
            result = BuildResult(
                buildId = "b1",
                projectName = "app",
                status = BuildStatus.SUCCESS,
                durationMs = 5000L,
                startedAt = 1000L,
                finishedAt = 6000L,
            ),
        )

        val result = mapper.map(payload)

        val event = assertIs<BuildResultEvent>(result)
        assertEquals("b1", event.buildId)
        assertEquals("app", event.projectName)
        assertEquals(FinishStatus.SUCCESS, event.status)
        assertEquals(5000L, event.durationMs)
        assertEquals(1000L, event.startedAt)
        assertEquals(6000L, event.finishedAt)
        assertTrue(event.errors.isEmpty())
        assertTrue(event.warnings.isEmpty())
    }

    @Test
    fun buildResultPayload_mapsFailedStatus() {
        val payload = BuildResultPayload(
            result = BuildResult(
                buildId = "b1",
                projectName = "app",
                status = BuildStatus.FAILED,
                durationMs = 3000L,
                startedAt = 1000L,
                finishedAt = 4000L,
            ),
        )

        val event = assertIs<BuildResultEvent>(mapper.map(payload))

        assertEquals(FinishStatus.FAILED, event.status)
    }

    @Test
    fun buildResultPayload_mapsCancelledStatus() {
        val payload = BuildResultPayload(
            result = BuildResult(
                buildId = "b1",
                projectName = "app",
                status = BuildStatus.CANCELLED,
                durationMs = 1000L,
                startedAt = 1000L,
                finishedAt = 2000L,
            ),
        )

        val event = assertIs<BuildResultEvent>(mapper.map(payload))

        assertEquals(FinishStatus.CANCELLED, event.status)
    }

    @Test
    fun buildResultPayload_transformsWireIssuesToDomain() {
        val payload = BuildResultPayload(
            result = BuildResult(
                buildId = "b1",
                projectName = "app",
                status = BuildStatus.FAILED,
                durationMs = 3000L,
                startedAt = 1000L,
                finishedAt = 4000L,
                errors = listOf(
                    WireBuildIssue(
                        message = "compilation error",
                        filePath = "Foo.kt",
                        line = 10,
                        column = 5,
                        severity = WireBuildIssue.Severity.ERROR,
                    ),
                ),
                warnings = listOf(
                    WireBuildIssue(
                        message = "deprecated call",
                        filePath = "Bar.kt",
                        line = 20,
                        column = 3,
                        severity = WireBuildIssue.Severity.WARNING,
                    ),
                ),
            ),
        )

        val event = assertIs<BuildResultEvent>(mapper.map(payload))

        assertEquals(1, event.errors.size)
        assertEquals("compilation error", event.errors[0].message)
        assertEquals("Foo.kt", event.errors[0].filePath)
        assertEquals(10, event.errors[0].line)

        assertEquals(1, event.warnings.size)
        assertEquals("deprecated call", event.warnings[0].message)
        assertEquals("Bar.kt", event.warnings[0].filePath)
        assertEquals(20, event.warnings[0].line)
    }

    // endregion

    // region BuildSnapshotPayload

    @Test
    fun snapshotPayload_producesSnapshotEvent() {
        val payload = BuildSnapshotPayload(
            activeBuilds = listOf(
                ActiveBuildInfo(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1000L,
                    tasks = listOf(
                        ActiveTaskInfo(":prepare", TaskStatus.SUCCESS),
                        ActiveTaskInfo(":compile", TaskStatus.RUNNING),
                    ),
                ),
            ),
        )

        val result = mapper.map(payload)

        val event = assertIs<SnapshotEvent>(result)
        assertEquals(1, event.activeBuilds.size)
        assertEquals("b1", event.activeBuilds[0].buildId)
        assertEquals("app", event.activeBuilds[0].projectName)
        assertEquals(1000L, event.activeBuilds[0].startedAt)
        assertEquals(":compile", event.activeBuilds[0].currentTask)
        assertTrue(event.recentResults.isEmpty())
    }

    @Test
    fun snapshotPayload_currentTaskIsNullWhenNoRunning() {
        val payload = BuildSnapshotPayload(
            activeBuilds = listOf(
                ActiveBuildInfo(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1000L,
                    tasks = listOf(
                        ActiveTaskInfo(":prepare", TaskStatus.SUCCESS),
                        ActiveTaskInfo(":compile", TaskStatus.UP_TO_DATE),
                    ),
                ),
            ),
        )

        val event = assertIs<SnapshotEvent>(mapper.map(payload))

        assertNull(event.activeBuilds[0].currentTask)
    }

    @Test
    fun snapshotPayload_emptyActiveBuilds() {
        val payload = BuildSnapshotPayload(activeBuilds = emptyList())

        val event = assertIs<SnapshotEvent>(mapper.map(payload))

        assertTrue(event.activeBuilds.isEmpty())
        assertTrue(event.recentResults.isEmpty())
    }

    @Test
    fun snapshotPayload_mapsRecentResults() {
        val payload = BuildSnapshotPayload(
            activeBuilds = listOf(
                ActiveBuildInfo(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1000L,
                    tasks = emptyList(),
                ),
            ),
            recentResults = listOf(
                BuildResult(
                    buildId = "b2",
                    projectName = "lib",
                    status = BuildStatus.FAILED,
                    durationMs = 2000L,
                    errors = listOf(
                        WireBuildIssue(
                            message = "link error",
                            filePath = null,
                            line = null,
                            column = null,
                            severity = WireBuildIssue.Severity.ERROR,
                        ),
                    ),
                    warnings = emptyList(),
                    startedAt = 500L,
                    finishedAt = 2500L,
                ),
            ),
        )

        val event = assertIs<SnapshotEvent>(mapper.map(payload))

        assertEquals(1, event.recentResults.size)
        val r = event.recentResults[0]
        assertEquals("b2", r.buildId)
        assertEquals(FinishStatus.FAILED, r.status)
        assertEquals(2000L, r.durationMs)
        assertEquals(1, r.errors.size)
        assertEquals("link error", r.errors[0].message)
    }

    // endregion

    // region Non-build payloads

    @Test
    fun heartbeatPayload_returnsNull() {
        assertNull(mapper.map(HeartbeatPayload()))
    }

    @Test
    fun helloPayload_returnsNull() {
        assertNull(
            mapper.map(
                HelloPayload(
                    deviceName = "Phone",
                    platform = "Android",
                    appVersion = "1.0",
                ),
            ),
        )
    }

    @Test
    fun commandResultPayload_returnsNull() {
        assertNull(
            mapper.map(
                CommandResultPayload(status = CommandStatus.ACCEPTED),
            ),
        )
    }

    // endregion
}
