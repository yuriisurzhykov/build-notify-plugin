package me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionManager
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost

class FakeConnectionManager(
    initial: ConnectionState = ConnectionState.Idle,
) : ConnectionManager {

    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<ConnectionState> = _state.asStateFlow()

    override suspend fun connect(host: DiscoveredHost) {
        _state.value = ConnectionState.Connecting(host)
    }

    override suspend fun disconnect() {
        _state.value = ConnectionState.Disconnected
    }

    fun setState(value: ConnectionState) {
        _state.update { value }
    }
}
