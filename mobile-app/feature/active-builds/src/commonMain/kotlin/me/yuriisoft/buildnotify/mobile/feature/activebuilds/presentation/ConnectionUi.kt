package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

/** Connection shown in the home top bar. */
@Immutable
sealed interface ConnectionUi {
    @Immutable
    data class Connected(val hostName: String) : ConnectionUi

    @Immutable
    data object Reconnecting : ConnectionUi

    @Immutable
    data object Offline : ConnectionUi
}
