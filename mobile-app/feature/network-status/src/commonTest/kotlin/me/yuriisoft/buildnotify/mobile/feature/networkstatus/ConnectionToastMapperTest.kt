package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.network.error.ConnectionErrorReason
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConnectionToastMapperTest {

    private val host = DiscoveredHost(name = "TestIDE", host = "192.168.1.1", port = 8443)

    @Test
    fun connectedToReconnectingShowsErrorToast() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Connected(host),
            current = ConnectionState.Reconnecting(host, attempt = 1),
        )

        assertEquals(1, actions.size)
        val show = assertIs<ToastAction.Show>(actions.single())
        assertEquals(ToastType.Error, show.data.type)
        assertIs<ToastDuration.Indefinite>(show.data.duration)
        assertEquals("network-status", show.data.id)
    }

    @Test
    fun reconnectingToConnectedDismissesAndShowsSuccess() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 3),
            current = ConnectionState.Connected(host),
        )

        assertEquals(2, actions.size)
        val dismiss = assertIs<ToastAction.Dismiss>(actions[0])
        assertEquals("network-status", dismiss.id)

        val show = assertIs<ToastAction.Show>(actions[1])
        assertEquals(ToastType.Success, show.data.type)
    }

    @Test
    fun reconnectingToFailedDismissesAndNavigatesToDiscovery() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 5),
            current = ConnectionState.Failed(host, ConnectionErrorReason.Timeout("timed out")),
        )

        assertEquals(2, actions.size)
        assertIs<ToastAction.Dismiss>(actions[0])
        assertIs<ToastAction.NavigateToDiscovery>(actions[1])
    }

    @Test
    fun reconnectingToDisconnectedDismissesAndNavigatesToDiscovery() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 2),
            current = ConnectionState.Disconnected,
        )

        assertEquals(2, actions.size)
        assertIs<ToastAction.Dismiss>(actions[0])
        assertIs<ToastAction.NavigateToDiscovery>(actions[1])
    }

    @Test
    fun idleToConnectingProducesNoActions() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Idle,
            current = ConnectionState.Connecting(host),
        )

        assertTrue(actions.isEmpty())
    }

    @Test
    fun connectingToConnectedProducesNoActions() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Connecting(host),
            current = ConnectionState.Connected(host),
        )

        assertTrue(actions.isEmpty())
    }

    @Test
    fun connectedToDisconnectedProducesNoActions() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Connected(host),
            current = ConnectionState.Disconnected,
        )

        assertTrue(actions.isEmpty())
    }

    @Test
    fun disconnectedToConnectingProducesNoActions() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Disconnected,
            current = ConnectionState.Connecting(host),
        )

        assertTrue(actions.isEmpty())
    }

    @Test
    fun sameStateTransitionProducesNoActions() {
        val connected = ConnectionState.Connected(host)
        val actions = ConnectionToastMapper.mapTransition(
            previous = connected,
            current = connected,
        )

        assertTrue(actions.isEmpty())
    }

    @Test
    fun reconnectingToFailedWithClientRejectedStillNavigatesToDiscovery() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 1),
            current = ConnectionState.Failed(host, ConnectionErrorReason.ClientRejected("rejected")),
        )

        assertEquals(2, actions.size)
        assertIs<ToastAction.Dismiss>(actions[0])
        assertIs<ToastAction.NavigateToDiscovery>(actions[1])
    }

    @Test
    fun dismissActionCarriesNetworkStatusId() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 1),
            current = ConnectionState.Connected(host),
        )

        val dismiss = assertIs<ToastAction.Dismiss>(actions.first())
        assertEquals("network-status", dismiss.id)
    }

    @Test
    fun successToastAfterReconnectionIsTimedDuration() {
        val actions = ConnectionToastMapper.mapTransition(
            previous = ConnectionState.Reconnecting(host, attempt = 1),
            current = ConnectionState.Connected(host),
        )

        val show = assertIs<ToastAction.Show>(actions[1])
        assertIs<ToastDuration.Timed>(show.data.duration)
    }
}
