package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeBuildEventStore
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeConnectionManager
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildCacheInvalidatorTest {

    private val host = DiscoveredHost(name = "ide", host = "127.0.0.1", port = 17337)

    @Test
    fun transitionToDisconnected_dropsActiveBuilds() = runTest {
        val (connection, store) = invalidator()

        connection.setState(ConnectionState.Connected(host))
        connection.setState(ConnectionState.Disconnected)

        assertEquals(1, store.dropCount)
    }

    @Test
    fun transitionToFailed_dropsActiveBuilds() = runTest {
        val (connection, store) = invalidator()

        connection.setState(ConnectionState.Connected(host))
        connection.setState(ConnectionState.Failed(host, ConnectionErrorReason.Unknown("test")))

        assertEquals(1, store.dropCount)
    }

    @Test
    fun transitionToReconnecting_keepsBuilds() = runTest {
        val (connection, store) = invalidator()

        connection.setState(ConnectionState.Connected(host))
        connection.setState(ConnectionState.Reconnecting(host, attempt = 1L))

        assertEquals(0, store.dropCount)
    }

    @Test
    fun whileConnected_keepsBuilds() = runTest {
        val (connection, store) = invalidator()

        connection.setState(ConnectionState.Connecting(host))
        connection.setState(ConnectionState.Connected(host))

        assertEquals(0, store.dropCount)
    }

    @Test
    fun repeatedTerminalState_dropsOnce() = runTest {
        val (connection, store) = invalidator()

        connection.setState(ConnectionState.Connected(host))
        connection.setState(ConnectionState.Disconnected)
        connection.setState(ConnectionState.Disconnected)

        assertEquals(1, store.dropCount)
    }

    private fun TestScope.invalidator(): Pair<FakeConnectionManager, FakeBuildEventStore> {
        val connection = FakeConnectionManager()
        val store = FakeBuildEventStore()
        BuildCacheInvalidator(
            connectionManager = connection,
            store = store,
            dispatchers = FakeAppDispatchers(UnconfinedTestDispatcher(testScheduler)),
            scope = backgroundScope,
        )
        return connection to store
    }
}
