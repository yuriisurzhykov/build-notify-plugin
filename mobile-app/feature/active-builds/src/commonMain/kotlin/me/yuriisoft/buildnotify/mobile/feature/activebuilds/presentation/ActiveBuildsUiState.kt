package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ActiveBuildsUiState {

    /** Before the first emission of the local cache. */
    @Immutable
    data object Loading : ActiveBuildsUiState

    /** Neither running nor recently finished builds are known. */
    @Immutable
    data object Empty : ActiveBuildsUiState

    /**
     * [streamFailed] keeps the last known lists visible with an inline banner instead
     * of replacing them with an error screen.
     */
    @Immutable
    data class Loaded(
        val running: ImmutableList<RunningBuildUi>,
        val finished: ImmutableList<FinishedBuildUi>,
        val streamFailed: Boolean = false,
        /** Open "hand off to an agent" sheet, or `null` when closed. */
        val handoff: HandoffUi? = null,
    ) : ActiveBuildsUiState
}
