package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeLocalActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeLocalBuildLogSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeRemoteActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeRemoteBuildLogSource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActiveBuildRepositoryTest {

    @Test
    fun observeBuilds_initially_emitsEmptyListFromLocal() = runTest {
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val repository = repository(
            buildsRemote = buildsRemote,
            buildsLocal = buildsLocal,
            logsRemote = logsRemote,
            logsLocal = logsLocal,
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeBuilds().collect { emissions.add(it) }
        }

        assertTrue(emissions.first().isEmpty())
    }

    @Test
    fun observeBuilds_afterRemoteEmission_persistsToLocalAndSubscriberSeesSameData() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val repository = repository(
            buildsRemote = buildsRemote,
            buildsLocal = buildsLocal,
            logsRemote = logsRemote,
            logsLocal = logsLocal,
        )

        val expected = listOf(
            BuildSnapshot.Active(
                buildId = "b1",
                projectName = "app",
                startedAt = 42L,
                currentTask = ":compile",
            ),
        )

        val emissions = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeBuilds().collect { emissions.add(it) }
        }

        buildsRemote.emit(expected)

        assertContentEquals(expected, buildsLocal.stored.value)
        assertContentEquals(expected, emissions.last())
    }

    @Test
    fun observeLogs_initially_emitsEmptyListForBuildId() = runTest {
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val repository = repository(
            buildsRemote = buildsRemote,
            buildsLocal = buildsLocal,
            logsRemote = logsRemote,
            logsLocal = logsLocal,
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeLogs("b1").collect { emissions.add(it) }
        }

        assertTrue(emissions.first().isEmpty())
    }

    @Test
    fun observeLogs_afterRemoteEmission_persistsToLocalAndSubscriberSeesSameData() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val repository = repository(
            buildsRemote = buildsRemote,
            buildsLocal = buildsLocal,
            logsRemote = logsRemote,
            logsLocal = logsLocal,
        )

        val expected = listOf(
            BuildLogEntry(timestamp = 1L, message = "> Task :app:compile", kind = LogKind.TASK),
        )

        val emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeLogs("b1").collect { emissions.add(it) }
        }

        logsRemote.emit("b1", expected)

        assertContentEquals(expected, emissions.last())
    }

    @Test
    fun observeBuilds_delegatesToBuildsCachedSourceObserveUnit() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val buildsCached = ActiveBuildCachedSource(buildsRemote, buildsLocal)
        val logsCached = BuildLogCachedSource(
            FakeRemoteBuildLogSource(),
            FakeLocalBuildLogSource(),
        )
        val repository = ActiveBuildRepository(
            buildsCached = buildsCached,
            logsCached = logsCached,
        )

        val fromRepository = mutableListOf<List<BuildSnapshot>>()
        val fromCached = mutableListOf<List<BuildSnapshot>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeBuilds().collect { fromRepository.add(it) }
        }
        backgroundScope.launch(testDispatcher) {
            buildsCached.observe(Unit).collect { fromCached.add(it) }
        }

        val payload = listOf(
            BuildSnapshot.Active(
                buildId = "b-delegate",
                projectName = "proj",
                startedAt = 99L,
                currentTask = null,
            ),
        )
        buildsRemote.emit(payload)

        assertContentEquals(fromCached.last(), fromRepository.last())
    }

    @Test
    fun observeLogs_delegatesToLogsCachedSourceObserveBuildId() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val buildsCached = ActiveBuildCachedSource(
            FakeRemoteActiveBuildSource(),
            FakeLocalActiveBuildSource(),
        )
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val logsCached = BuildLogCachedSource(logsRemote, logsLocal)
        val repository = ActiveBuildRepository(
            buildsCached = buildsCached,
            logsCached = logsCached,
        )

        val fromRepository = mutableListOf<List<BuildLogEntry>>()
        val fromCached = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeLogs("target").collect { fromRepository.add(it) }
        }
        backgroundScope.launch(testDispatcher) {
            logsCached.observe("target").collect { fromCached.add(it) }
        }

        val payload = listOf(
            BuildLogEntry(timestamp = 7L, message = "delegated", kind = LogKind.ERROR),
        )
        logsRemote.emit("target", payload)

        assertContentEquals(fromCached.last(), fromRepository.last())
    }

    @Test
    fun observeLogs_isolatedPerBuildId() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val buildsRemote = FakeRemoteActiveBuildSource()
        val buildsLocal = FakeLocalActiveBuildSource()
        val logsRemote = FakeRemoteBuildLogSource()
        val logsLocal = FakeLocalBuildLogSource()
        val repository = repository(
            buildsRemote = buildsRemote,
            buildsLocal = buildsLocal,
            logsRemote = logsRemote,
            logsLocal = logsLocal,
        )

        val forB1 = listOf(BuildLogEntry(1L, "b1-line", LogKind.TASK))
        val forB2 = listOf(BuildLogEntry(2L, "b2-line", LogKind.WARNING))

        val b1Emissions = mutableListOf<List<BuildLogEntry>>()
        val b2Emissions = mutableListOf<List<BuildLogEntry>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeLogs("b1").collect { b1Emissions.add(it) }
        }
        backgroundScope.launch(testDispatcher) {
            repository.observeLogs("b2").collect { b2Emissions.add(it) }
        }

        logsRemote.emit("b1", forB1)
        logsRemote.emit("b2", forB2)

        assertContentEquals(forB1, b1Emissions.last())
        assertContentEquals(forB2, b2Emissions.last())
        assertEquals(1, b1Emissions.last().size)
        assertEquals(1, b2Emissions.last().size)
    }

    private fun repository(
        buildsRemote: FakeRemoteActiveBuildSource,
        buildsLocal: FakeLocalActiveBuildSource,
        logsRemote: FakeRemoteBuildLogSource,
        logsLocal: FakeLocalBuildLogSource,
    ): ActiveBuildRepository {
        val buildsCached = ActiveBuildCachedSource(buildsRemote, buildsLocal)
        val logsCached = BuildLogCachedSource(logsRemote, logsLocal)
        return ActiveBuildRepository(
            buildsCached = buildsCached,
            logsCached = logsCached,
        )
    }
}
