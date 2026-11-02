package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository

class BuildLogsViewModel(
    buildId: String,
    repository: IActiveBuildRepository,
    private val mapper: BuildLogsUiMapper = BuildLogsUiMapper(),
    private val state: StateCommunication.Mutable<BuildLogsUiState> =
        StateCommunication(BuildLogsUiState.Loading),
) : ViewModel() {

    val uiState: StateFlow<BuildLogsUiState> = state.observe

    private val filter = MutableStateFlow(LogFilter.ALL)

    init {
        val build = repository.observeBuilds()
            .map { builds -> builds.firstOrNull { it.buildId == buildId } }
            .distinctUntilChanged()

        combine(build, repository.observeLogs(buildId), filter) { snapshot, logs, filter ->
            mapper.map(snapshot, logs, filter)
        }
            .onEach { state.put(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                // Keep whatever was last shown; a broken stream must not blank the screen.
                if (state.observe.value is BuildLogsUiState.Loading) {
                    state.put(BuildLogsUiState.NotFound)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: BuildLogsIntent) {
        when (intent) {
            is BuildLogsIntent.SelectFilter -> filter.value = intent.filter
        }
    }
}
