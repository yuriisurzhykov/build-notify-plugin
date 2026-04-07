package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.remote

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveBuildInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.ActiveTaskInfo
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildSnapshotPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStatus
import me.yuriisoft.buildnotify.mobile.data.protocol.HeartbeatPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskFinishedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildEventMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeActiveSession
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteActiveBuildSourceTest {

    // region Initial state

    @Test
    fun initialEmission_isEmptyList() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        assertTrue(emissions.first().isEmpty())
    }

    // endregion

    // region BuildStartedPayload

    @Test
    fun buildStartedPayload_producesActiveSnapshot() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))

        val last = emissions.last()
        assertEquals(1, last.size)
        val active = assertIs<BuildSnapshot.Active>(last.first())
        assertEquals("b1", active.buildId)
        assertEquals("app", active.projectName)
        assertEquals(FIXED_TIME, active.startedAt)
        assertNull(active.currentTask)
    }

    // endregion

    // region TaskStartedPayload

    @Test
    fun taskStartedPayload_updatesCurrentTaskOnActiveBuild() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))
        session.emit(
            TaskStartedPayload(
                buildId = "b1",
                projectName = "app",
                taskPath = ":compile",
            ),
        )

        val active = assertIs<BuildSnapshot.Active>(emissions.last().single())
        assertEquals(":compile", active.currentTask)
    }

    @Test
    fun taskStartedForUnknownBuild_doesNotAddEntry() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(
            TaskStartedPayload(
                buildId = "unknown",
                projectName = "app",
                taskPath = ":compile",
            ),
        )

        assertTrue(emissions.last().isEmpty())
    }

    // endregion

    // region Full build lifecycle

    @Test
    fun fullBuildLifecycle_producesFinishedSnapshot() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))
        session.emit(
            TaskStartedPayload(
                buildId = "b1",
                projectName = "app",
                taskPath = ":compile",
            ),
        )
        session.emit(
            TaskFinishedPayload(
                buildId = "b1",
                projectName = "app",
                taskPath = ":compile",
                status = TaskStatus.SUCCESS,
            ),
        )
        session.emit(
            BuildResultPayload(
                result = BuildResult(
                    buildId = "b1",
                    projectName = "app",
                    status = BuildStatus.SUCCESS,
                    durationMs = 5000L,
                    startedAt = 1000L,
                    finishedAt = 6000L,
                ),
            ),
        )

        val last = emissions.last()
        assertEquals(1, last.size)
        val finished = assertIs<BuildSnapshot.Finished>(last.first())
        assertEquals("b1", finished.buildId)
        assertEquals(FinishStatus.SUCCESS, finished.outcome.status)
        assertEquals(5000L, finished.outcome.durationMs)
    }

    // endregion

    // region Non-build payloads

    @Test
    fun nonBuildPayloads_areFilteredOut() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(HeartbeatPayload())

        assertTrue(emissions.all { it.isEmpty() })
    }

    // endregion

    // region Multiple builds

    @Test
    fun multipleBuilds_accumulateIndependently() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))
        session.emit(BuildStartedPayload(buildId = "b2", projectName = "lib"))

        val last = emissions.last()
        assertEquals(2, last.size)
        assertEquals(setOf("b1", "b2"), last.map { it.buildId }.toSet())
    }

    // endregion

    // region SnapshotPayload

    @Test
    fun snapshotPayload_replacesEntireBuildMap() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(BuildStartedPayload(buildId = "b1", projectName = "app"))
        assertEquals(1, emissions.last().size)
        assertEquals("b1", emissions.last().first().buildId)

        session.emit(
            BuildSnapshotPayload(
                activeBuilds = listOf(
                    ActiveBuildInfo(
                        buildId = "b2",
                        projectName = "lib",
                        startedAt = 2000L,
                        tasks = emptyList(),
                    ),
                ),
            ),
        )

        val last = emissions.last()
        assertEquals(1, last.size)
        assertEquals("b2", last.first().buildId)
    }

    @Test
    fun snapshotPayload_includesRecentResults() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = FakeActiveSession()
        val source = RemoteActiveBuildSource(
            session = session,
            mapper = BuildEventMapper(clock = { FIXED_TIME }),
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            source.observe(Unit).collect { emissions.add(it) }
        }

        session.emit(
            BuildSnapshotPayload(
                activeBuilds = listOf(
                    ActiveBuildInfo(
                        buildId = "b1",
                        projectName = "app",
                        startedAt = 1000L,
                        tasks = listOf(
                            ActiveTaskInfo(":compile", TaskStatus.RUNNING),
                        ),
                    ),
                ),
                recentResults = listOf(
                    BuildResult(
                        buildId = "b2",
                        projectName = "lib",
                        status = BuildStatus.SUCCESS,
                        durationMs = 3000L,
                        startedAt = 500L,
                        finishedAt = 3500L,
                    ),
                ),
            ),
        )

        val last = emissions.last()
        assertEquals(2, last.size)

        val active = last.first { it.buildId == "b1" }
        assertIs<BuildSnapshot.Active>(active)
        assertEquals(":compile", active.currentTask)

        val finished = last.first { it.buildId == "b2" }
        assertIs<BuildSnapshot.Finished>(finished)
        assertEquals(FinishStatus.SUCCESS, finished.outcome.status)
    }

    // endregion

    companion object {
        private const val FIXED_TIME = 1_700_000_000_000L
    }
}
