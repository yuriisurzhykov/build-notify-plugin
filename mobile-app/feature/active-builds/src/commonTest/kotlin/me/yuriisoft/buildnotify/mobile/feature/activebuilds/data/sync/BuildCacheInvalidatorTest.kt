package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeConnectionManager
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeLocalActiveBuildSource
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildCacheInvalidatorTest {

    private val testHost = DiscoveredHost(name = "ide", host = "127.0.0.1", port = 17337)

    private class TrackingBuilds(
        private val inner: FakeLocalActiveBuildSource,
    ) : MutableDataSource<Unit, List<BuildSnapshot>> {
        var deleteCount = 0
        override fun observe(params: Unit) = inner.observe(params)
        override suspend fun save(params: Unit, data: List<BuildSnapshot>) = inner.save(params, data)
        override suspend fun delete(params: Unit) {
            deleteCount++
            inner.delete(params)
        }
    }

    @Test
    fun transitionToDisconnected_clearsBuilds() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val connection = FakeConnectionManager()
        val localBuilds = FakeLocalActiveBuildSource()
        val trackingBuilds = TrackingBuilds(localBuilds)

        trackingBuilds.save(
            Unit,
            listOf(BuildSnapshot.Active(buildId = "b1", projectName = "app", startedAt = 1L, currentTask = null)),
        )

        BuildCacheInvalidator(
            connectionManager = connection,
            localBuilds = trackingBuilds,
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        connection.setState(ConnectionState.Connected(testHost))
        connection.setState(ConnectionState.Disconnected)

        assertEquals(1, trackingBuilds.deleteCount)
        assertTrue(localBuilds.stored.value.isEmpty())
    }

    @Test
    fun transitionToFailed_clearsBuilds() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val connection = FakeConnectionManager()
        val trackingBuilds = TrackingBuilds(FakeLocalActiveBuildSource())

        trackingBuilds.save(
            Unit,
            listOf(BuildSnapshot.Active(buildId = "b2", projectName = "p", startedAt = 2L, currentTask = null)),
        )

        BuildCacheInvalidator(
            connectionManager = connection,
            localBuilds = trackingBuilds,
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        connection.setState(ConnectionState.Connected(testHost))
        connection.setState(ConnectionState.Failed(testHost, ConnectionErrorReason.Unknown("test")))

        assertEquals(1, trackingBuilds.deleteCount)
    }

    @Test
    fun transitionToReconnecting_doesNotClear() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val connection = FakeConnectionManager()
        val localBuilds = FakeLocalActiveBuildSource()
        val trackingBuilds = TrackingBuilds(localBuilds)

        trackingBuilds.save(
            Unit,
            listOf(BuildSnapshot.Active(buildId = "b", projectName = "p", startedAt = 0L, currentTask = null)),
        )

        BuildCacheInvalidator(
            connectionManager = connection,
            localBuilds = trackingBuilds,
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        connection.setState(ConnectionState.Connected(testHost))
        connection.setState(ConnectionState.Reconnecting(testHost, attempt = 1L))

        assertEquals(0, trackingBuilds.deleteCount)
        assertEquals(1, localBuilds.stored.value.size)
    }

    @Test
    fun whileConnected_doesNotClear() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val connection = FakeConnectionManager()
        val localBuilds = FakeLocalActiveBuildSource()
        val trackingBuilds = TrackingBuilds(localBuilds)

        trackingBuilds.save(
            Unit,
            listOf(BuildSnapshot.Active(buildId = "b", projectName = "p", startedAt = 0L, currentTask = null)),
        )

        BuildCacheInvalidator(
            connectionManager = connection,
            localBuilds = trackingBuilds,
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        connection.setState(ConnectionState.Connecting(testHost))
        connection.setState(ConnectionState.Connected(testHost))

        assertEquals(0, trackingBuilds.deleteCount)
    }

    @Test
    fun alreadyInTerminalState_doesNotClearAgainOnRepeat() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val connection = FakeConnectionManager()
        val trackingBuilds = TrackingBuilds(FakeLocalActiveBuildSource())

        BuildCacheInvalidator(
            connectionManager = connection,
            localBuilds = trackingBuilds,
            dispatchers = FakeAppDispatchers(testDispatcher),
            scope = backgroundScope,
        )

        connection.setState(ConnectionState.Connected(testHost))
        connection.setState(ConnectionState.Disconnected)
        connection.setState(ConnectionState.Disconnected)

        assertEquals(1, trackingBuilds.deleteCount)
    }
}
