package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository

/**
 * Shows one build's log through a [LogWindow]: only the newest page is read, and the
 * reader pulls older pages by scrolling up. Counts come from SQL aggregates, so a log
 * of any length costs the same to open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BuildLogsViewModel(
    buildId: String,
    repository: IActiveBuildRepository,
    private val mapper: BuildLogsUiMapper = BuildLogsUiMapper(),
    private val state: StateCommunication.Mutable<BuildLogsUiState> =
        StateCommunication(BuildLogsUiState.Loading),
) : ViewModel() {

    val uiState: StateFlow<BuildLogsUiState> = state.observe

    private val window = MutableStateFlow(LogWindow.Initial)

    init {
        val build = repository.observeBuilds()
            .map { builds -> builds.firstOrNull { it.buildId == buildId } }
            .distinctUntilChanged()

        val lines = window.flatMapLatest { current ->
            repository.observeLogs(LogQuery(buildId, current.filter.kinds, current.limit))
                .map { current to it }
        }

        combine(build, repository.observeLogStats(buildId), lines) { snapshot, stats, (shown, logs) ->
            mapper.map(snapshot, logs, stats, shown.filter)
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
            is BuildLogsIntent.SelectFilter -> window.update { it.withFilter(intent.filter) }
            is BuildLogsIntent.LoadOlder    -> loadOlder()
        }
    }

    private fun loadOlder() {
        val loaded = state.observe.value as? BuildLogsUiState.Loaded ?: return
        if (loaded.hasOlder) window.update { it.older() }
    }
}
