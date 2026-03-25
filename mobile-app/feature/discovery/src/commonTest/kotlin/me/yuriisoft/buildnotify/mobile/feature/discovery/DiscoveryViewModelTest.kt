package me.yuriisoft.buildnotify.mobile.feature.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.yuriisoft.buildnotify.mobile.domain.model.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.feature.discovery.domain.ObserveHostsUseCase
import me.yuriisoft.buildnotify.mobile.feature.discovery.presentation.DiscoveryEvent
import me.yuriisoft.buildnotify.mobile.feature.discovery.presentation.DiscoveryUiState
import me.yuriisoft.buildnotify.mobile.feature.discovery.presentation.DiscoveryViewModel
import me.yuriisoft.buildnotify.mobile.testing.FakeNsdRepository
import me.yuriisoft.buildnotify.mobile.testing.RecordingEventCommunication
import me.yuriisoft.buildnotify.mobile.testing.RecordingStateCommunication
import me.yuriisoft.buildnotify.mobile.testing.TestAppDispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private val repository = FakeNsdRepository()
    private val dispatchers = TestAppDispatchers()
    private val useCase = ObserveHostsUseCase(repository)
    private val state = RecordingStateCommunication<DiscoveryUiState>(DiscoveryUiState.Loading)
    private val events = RecordingEventCommunication<DiscoveryEvent>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatchers.main)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DiscoveryViewModel(useCase, dispatchers, state, events)

    @Test
    fun collectsFromRepositoryImmediatelyOnCreation() {
        val vm = createViewModel()

        assertIs<DiscoveryUiState.Content>(vm.uiState.value)
    }

    @Test
    fun showsHostsWhenDiscoverySucceeds() {
        val hosts = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
        )
        repository.emit(hosts)

        val vm = createViewModel()
        val currentState = vm.uiState.value

        assertIs<DiscoveryUiState.Content>(currentState)
        assertEquals(hosts, currentState.hosts)
    }

    @Test
    fun showsMultipleDiscoveredHosts() {
        val hosts = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
            DiscoveredHost(name = "Desktop", host = "192.168.1.10", port = 8766),
            DiscoveredHost(name = "Linux", host = "10.0.0.1", port = 9000),
        )
        repository.emit(hosts)

        val vm = createViewModel()
        val currentState = vm.uiState.value

        assertIs<DiscoveryUiState.Content>(currentState)
        assertEquals(3, currentState.hosts.size)
        assertEquals(hosts, currentState.hosts)
    }

    @Test
    fun updatesStateWhenHostsChange() {
        val initial = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
        )
        repository.emit(initial)

        val vm = createViewModel()

        val updated = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
            DiscoveredHost(name = "Desktop", host = "192.168.1.10", port = 8766),
        )
        repository.emit(updated)

        val currentState = vm.uiState.value
        assertIs<DiscoveryUiState.Content>(currentState)
        assertEquals(updated, currentState.hosts)
    }

    @Test
    fun showsEmptyContentWhenNoHostsFound() {
        repository.emit(emptyList())

        val vm = createViewModel()
        val currentState = vm.uiState.value

        assertIs<DiscoveryUiState.Content>(currentState)
        assertEquals(emptyList(), currentState.hosts)
    }

    @Test
    fun emitsNavigateToBuildEventOnHostSelection() {
        val host = DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765)
        repository.emit(listOf(host))

        val vm = createViewModel()
        vm.selectHost(host)

        assertEquals(1, events.history.size)
        val event = events.history.first()
        assertIs<DiscoveryEvent.NavigateToBuild>(event)
        assertEquals("192.168.1.5", event.host)
        assertEquals(8765, event.port)
    }

    @Test
    fun verifyStateTransitionOrder() {
        val hosts1 = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
        )
        val hosts2 = listOf(
            DiscoveredHost(name = "MacBook", host = "192.168.1.5", port = 8765),
            DiscoveredHost(name = "Desktop", host = "192.168.1.10", port = 8766),
        )
        repository.emit(hosts1)

        createViewModel()

        repository.emit(hosts2)

        assertEquals(
            listOf(
                DiscoveryUiState.Loading,
                DiscoveryUiState.Content(hosts1),
                DiscoveryUiState.Content(hosts2),
            ),
            state.history,
        )
    }
}
