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
}

@Immutable
sealed interface ActiveBuildsUiEvent {

    @Immutable
    data class NavigateToLogs(val buildId: String) : ActiveBuildsUiEvent

    @Immutable
    data object CancelFailed : ActiveBuildsUiEvent
}
