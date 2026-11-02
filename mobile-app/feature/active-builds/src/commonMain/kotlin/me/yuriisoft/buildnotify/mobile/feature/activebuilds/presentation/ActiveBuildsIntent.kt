package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ActiveBuildsIntent {

    @Immutable
    data class StopBuild(val buildId: String) : ActiveBuildsIntent

    @Immutable
    data class OpenLogs(val buildId: String) : ActiveBuildsIntent

    @Immutable
    data object RetryStream : ActiveBuildsIntent

    /** Open the "hand off to an agent" sheet for a failed build. */
    @Immutable
    data class OpenHandoff(val buildId: String) : ActiveBuildsIntent

    @Immutable
    data object CloseHandoff : ActiveBuildsIntent

    @Immutable
    data object ToggleHandoffLog : ActiveBuildsIntent
}

@Immutable
sealed interface ActiveBuildsUiEvent {

    @Immutable
    data class NavigateToLogs(val buildId: String) : ActiveBuildsUiEvent

    @Immutable
    data object CancelFailed : ActiveBuildsUiEvent
}
