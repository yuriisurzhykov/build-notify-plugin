package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveBuildInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildDiagnosticPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildSnapshotPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.HeartbeatPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildEventMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeActiveSession
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.yuriisoft.buildnotify.mobile.data.protocol.DiagnosticSeverity as WireSeverity

class RemoteBuildLogSourceTest {

    // region Initial state

    @Test
    fun initialEmission_isEmptyList() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        assertTrue(emissions.first().isEmpty())
    }

    // endregion

    // region TaskStartedPayload

    @Test
    fun taskStartedPayload_appendsTaskLogEntry() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(
            TaskStartedPayload(
                buildId = "b1",
                projectName = "app",
                taskPath = ":compile",
            ),
        )

        val logs = emissions.last()
        assertEquals(1, logs.size)
        assertEquals(":compile", logs[0].message)
        assertEquals(LogKind.TASK, logs[0].kind)
        assertEquals(FIXED_TIME, logs[0].timestamp)
    }

    // endregion

    // region DiagnosticPayload

    @Test
    fun diagnosticPayload_appendsWarningLogEntry() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(
            BuildDiagnosticPayload(
                buildId = "b1",
                projectName = "app",
                severity = WireSeverity.WARNING,
                message = "deprecated API",
                filePath = "Foo.kt",
                line = 10,
            ),
        )

        val logs = emissions.last()
        assertEquals(1, logs.size)
        assertEquals("deprecated API", logs[0].message)
        assertEquals(LogKind.WARNING, logs[0].kind)
    }

    @Test
    fun diagnosticPayload_appendsErrorLogEntry() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(
            BuildDiagnosticPayload(
                buildId = "b1",
                projectName = "app",
                severity = WireSeverity.ERROR,
                message = "unresolved reference",
                filePath = "Bar.kt",
                line = 5,
            ),
        )

        val logs = emissions.last()
        assertEquals(1, logs.size)
        assertEquals("unresolved reference", logs[0].message)
        assertEquals(LogKind.ERROR, logs[0].kind)
    }

    // endregion

    // region Build-level filtering

    @Test
    fun observe_returnsOnlyLogsForRequestedBuildId() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val b1Emissions = mutableListOf<List<BuildLogEntry>>()
        val b2Emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { b1Emissions.add(it) }
        }
        backgroundScope.launch(testDispatcher) {
            source.observe("b2").collect { b2Emissions.add(it) }
        }

        session.emit(
            TaskStartedPayload(buildId = "b1", projectName = "app", taskPath = ":compile"),
        )
        session.emit(
            TaskStartedPayload(buildId = "b2", projectName = "lib", taskPath = ":test"),
        )

        assertEquals(1, b1Emissions.last().size)
        assertEquals(":compile", b1Emissions.last()[0].message)

        assertEquals(1, b2Emissions.last().size)
        assertEquals(":test", b2Emissions.last()[0].message)
    }

    // endregion

    // region Non-log-producing events

    @Test
    fun nonBuildPayloads_doNotAppendLogs() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(HeartbeatPayload())

        assertTrue(emissions.all { it.isEmpty() })
    }

    @Test
    fun buildStartedPayload_doesNotAppendLogs() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))

        assertTrue(emissions.all { it.isEmpty() })
    }

    // endregion

    // region Accumulation order

    @Test
    fun multipleEvents_accumulateInOrder() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        var time = 1000L
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { time }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(
            TaskStartedPayload(buildId = "b1", projectName = "app", taskPath = ":compile"),
        )
        time = 2000L
        session.emit(
            BuildDiagnosticPayload(
                buildId = "b1",
                projectName = "app",
                severity = WireSeverity.WARNING,
                message = "deprecation warning",
            ),
        )
        time = 3000L
        session.emit(
            BuildDiagnosticPayload(
                buildId = "b1",
                projectName = "app",
                severity = WireSeverity.ERROR,
                message = "compilation error",
            ),
        )

        val logs = emissions.last()
        assertEquals(3, logs.size)
        assertEquals(LogKind.TASK, logs[0].kind)
        assertEquals(1000L, logs[0].timestamp)
        assertEquals(LogKind.WARNING, logs[1].kind)
        assertEquals(2000L, logs[1].timestamp)
        assertEquals(LogKind.ERROR, logs[2].kind)
        assertEquals(3000L, logs[2].timestamp)
    }

    // endregion

    // region SnapshotPayload

    @Test
    fun snapshotPayload_clearsAllLogs() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteBuildLogSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            source.observe("b1").collect { emissions.add(it) }
        }

        session.emit(
            TaskStartedPayload(buildId = "b1", projectName = "app", taskPath = ":compile"),
        )
        assertEquals(1, emissions.last().size)

        session.emit(
            BuildSnapshotPayload(
                activeBuilds = listOf(
                    ActiveBuildInfo(
                        buildId = "b1",
                        projectName = "app",
                        startedAt = 1000L,
                        tasks = emptyList(),
                    ),
                ),
            ),
        )

        assertTrue(emissions.last().isEmpty())
    }

    // endregion

    companion object {
        private const val FIXED_TIME = 1_700_000_000_000L
    }
}
