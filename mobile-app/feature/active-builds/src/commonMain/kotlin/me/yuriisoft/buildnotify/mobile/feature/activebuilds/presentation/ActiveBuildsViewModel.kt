package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.yuriisoft.buildnotify.mobile.core.communication.EventCommunication
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.usecase.CancelBuildUseCase

class ActiveBuildsViewModel(
    private val repository: IActiveBuildRepository,
    private val cancelBuild: CancelBuildUseCase,
    private val dispatchers: AppDispatchers,
    private val mapper: ActiveBuildsUiMapper = ActiveBuildsUiMapper(),
    private val state: StateCommunication.Mutable<ActiveBuildsUiState> =
        StateCommunication(ActiveBuildsUiState.Loading),
    private val events: EventCommunication.Mutable<ActiveBuildsUiEvent> = EventCommunication(),
) : ViewModel() {

    val uiState: StateFlow<ActiveBuildsUiState> = state.observe
    val uiEvents: Flow<ActiveBuildsUiEvent> = events.observe

    private var observeJob: Job? = null

    init {
        observeBuilds()
    }

    fun onIntent(intent: ActiveBuildsIntent) {
        when (intent) {
            is ActiveBuildsIntent.StopBuild   -> stopBuild(intent.buildId)
            is ActiveBuildsIntent.OpenLogs    ->
                events.trySend(ActiveBuildsUiEvent.NavigateToLogs(intent.buildId))

            ActiveBuildsIntent.RetryStream    -> observeBuilds()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBuilds() {
        observeJob?.cancel()
        observeJob = repository.observeBuilds()
            .flatMapLatest { snapshots -> withRunningLogs(snapshots) }
            .map { (snapshots, logs) -> mapper.map(snapshots, logs) }
            .onEach { state.put(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                state.update { current ->
                    (current as? ActiveBuildsUiState.Loaded)?.copy(streamFailed = true)
                        ?: ActiveBuildsUiState.Loaded(
                            running = persistentListOf(),
                            finished = persistentListOf(),
                            streamFailed = true,
                        )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun withRunningLogs(
        snapshots: List<BuildSnapshot>,
    ): Flow<Pair<List<BuildSnapshot>, Map<String, List<BuildLogEntry>>>> {
        val runningIds = snapshots.filterIsInstance<BuildSnapshot.Active>().map { it.buildId }
        if (runningIds.isEmpty()) return flowOf(snapshots to emptyMap())
        return combine(runningIds.map { id -> repository.observeLogs(id).map { id to it } }) {
            snapshots to it.toMap()
        }
    }

    private fun stopBuild(buildId: String) {
        dispatchers.launchBackground(viewModelScope) {
            try {
                cancelBuild(buildId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                events.send(ActiveBuildsUiEvent.CancelFailed)
            }
        }
    }
}
