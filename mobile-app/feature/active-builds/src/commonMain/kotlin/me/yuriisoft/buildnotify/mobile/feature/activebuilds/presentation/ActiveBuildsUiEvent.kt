package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ActiveBuildsUiEvent {

    @Immutable
    data class NavigateToLogs(val buildId: String) : ActiveBuildsUiEvent

    @Immutable
    data object CancelFailed : ActiveBuildsUiEvent
}
