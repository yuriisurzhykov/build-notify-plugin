package me.yuriisoft.buildnotify.mobile.feature.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import me.yuriisoft.buildnotify.mobile.core.communication.EventCommunication
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.core.usecase.FlowUseCase
import me.yuriisoft.buildnotify.mobile.core.usecase.NoParams
import me.yuriisoft.buildnotify.mobile.domain.model.DiscoveredHost

class DiscoveryViewModel(
    private val observeHosts: FlowUseCase<NoParams, List<DiscoveredHost>>,
    private val dispatchers: AppDispatchers,
    private val state: StateCommunication.Mutable<DiscoveryUiState> = StateCommunication(
        DiscoveryUiState.Loading
    ),
    private val events: EventCommunication.Mutable<DiscoveryEvent> = EventCommunication(),
) : ViewModel() {

    val uiState: StateFlow<DiscoveryUiState> = state.observe
    val uiEvents: Flow<DiscoveryEvent> = events.observe

    init {
        startDiscovery()
    }

    fun selectHost(host: DiscoveredHost) {
        events.trySend(DiscoveryEvent.NavigateToBuild(host.host, host.port))
    }

    private fun startDiscovery() {
        dispatchers.launchBackground(viewModelScope) {
            observeHosts(NoParams)
                .catch { e -> state.put(DiscoveryUiState.Error(e.message.orEmpty())) }
                .collect { hosts -> state.put(DiscoveryUiState.Content(hosts)) }
        }
    }
}
