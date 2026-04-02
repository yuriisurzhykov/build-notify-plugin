package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.StateFlow
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.toast.ToastHostState

/**
 * Lifecycle-aware entry point that activates [NetworkStatusPipeline].
 *
 * The composable itself contains zero business logic — it only binds
 * the pipeline to the composition lifetime via [LaunchedEffect].
 * All domain knowledge lives in [ConnectionToastMapper] and the
 * polymorphic [ToastAction] subtypes.
 */
@Composable
fun NetworkStatusEffect(
    connectionState: StateFlow<ConnectionState>,
    toastHostState: ToastHostState,
    navigator: Navigator,
) {
    LaunchedEffect(connectionState) {
        NetworkStatusPipeline(
            connectionState = connectionState,
            toastHostState = toastHostState,
            navigator = navigator,
            scope = this
        ).activate()
    }
}
