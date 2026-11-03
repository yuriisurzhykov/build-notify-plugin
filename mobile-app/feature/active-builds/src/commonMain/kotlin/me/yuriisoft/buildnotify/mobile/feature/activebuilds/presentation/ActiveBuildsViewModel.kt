package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.yuriisoft.buildnotify.mobile.core.communication.EventCommunication
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.usecase.CancelBuildUseCase
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionManager
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveBuildsViewModel(
    private val repository: IActiveBuildRepository,
    private val cancelBuild: CancelBuildUseCase,
    private val dispatchers: AppDispatchers,
    connectionManager: ConnectionManager,
    private val mapper: ActiveBuildsUiMapper = ActiveBuildsUiMapper(),
    private val state: StateCommunication.Mutable<ActiveBuildsUiState> =
        StateCommunication(ActiveBuildsUiState.Loading),
    private val events: EventCommunication.Mutable<ActiveBuildsUiEvent> = EventCommunication(),
) : ViewModel() {

    val uiState: StateFlow<ActiveBuildsUiState> = state.observe
    val uiEvents: Flow<ActiveBuildsUiEvent> = events.observe

    val connection: StateFlow<ConnectionUi> = connectionManager.state
        .map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, connectionManager.state.value.toUi())

    /** Build whose hand-off sheet is open, and whether to attach its log. */
    private val handoffTarget = MutableStateFlow<String?>(null)
    private val handoffIncludeLog = MutableStateFlow(false)

    private var observeJob: Job? = null

    init {
        observeBuilds()
    }

    fun onIntent(intent: ActiveBuildsIntent) {
        when (intent) {
            is ActiveBuildsIntent.StopBuild     -> stopBuild(intent.buildId)
            is ActiveBuildsIntent.OpenLogs      ->
                events.trySend(ActiveBuildsUiEvent.NavigateToLogs(intent.buildId))

            is ActiveBuildsIntent.RetryStream      -> observeBuilds()
            is ActiveBuildsIntent.OpenHandoff   -> {
                handoffIncludeLog.value = false
                handoffTarget.value = intent.buildId
            }

            is ActiveBuildsIntent.CloseHandoff     -> handoffTarget.value = null
            is ActiveBuildsIntent.ToggleHandoffLog -> handoffIncludeLog.value = !handoffIncludeLog.value
        }
    }

    private fun observeBuilds() {
        observeJob?.cancel()

        val builds = repository.observeBuilds()
            .flatMapLatest { snapshots -> withRunningLogs(snapshots) }

        val handoff: Flow<HandoffRequest?> = handoffTarget.flatMapLatest { buildId ->
            if (buildId == null) {
                flowOf(null)
            } else {
                combine(
                    repository.observeLogs(LogQuery(buildId, ALL_KINDS, HandoffPrompt.MAX_LOG_LINES)),
                    repository.observeLogStats(buildId),
                    handoffIncludeLog,
                ) { logs, stats, include ->
                    HandoffRequest(buildId = buildId, includeLog = include, logs = logs, totalLines = stats.total)
                }
            }
        }

        observeJob = combine(builds, handoff) { (snapshots, logs), request ->
            mapper.map(snapshots, logs, request)
        }
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
        // Only the few lines each running card shows are read, never the whole log.
        val tails = runningIds.map { id ->
            repository.observeLogs(LogQuery(id, ALL_KINDS, mapper.recentLogLines)).map { id to it }
        }
        return combine(tails) {
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

private val ALL_KINDS: Set<LogKind> = LogKind.entries.toSet()

private fun ConnectionState.toUi(): ConnectionUi = when (this) {
    is ConnectionState.Connected    -> ConnectionUi.Connected(host.name)
    is ConnectionState.Reconnecting,
    is ConnectionState.Connecting,
    is ConnectionState.PairingRequired -> ConnectionUi.Reconnecting

    is ConnectionState.Idle,
    is ConnectionState.Disconnected,
    is ConnectionState.Failed       -> ConnectionUi.Offline
}
