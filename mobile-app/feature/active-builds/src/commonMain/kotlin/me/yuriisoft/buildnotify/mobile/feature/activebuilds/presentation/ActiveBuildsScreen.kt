package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.core.navigation.ScreenTransitions
import me.yuriisoft.buildnotify.mobile.core.navigation.routes.BuildLogsDestination
import me.yuriisoft.buildnotify.mobile.core.navigation.routes.BuildStatusDestination
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.error_cancel_build
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_copied
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui.ActiveBuildsContent
import me.yuriisoft.buildnotify.mobile.toast.LocalToastHostState
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource

/**
 * Home screen once connected: running builds with a live console tail, then recent results.
 * Owns the [BuildStatusDestination] route that discovery and the start route navigate to.
 */
@Inject
@Immutable
class ActiveBuildsScreen(
    private val viewModelFactory: () -> ActiveBuildsViewModel,
) : Screen() {

    override val destination = BuildStatusDestination
    override val transitions = ScreenTransitions.Fade

    @Composable
    override fun Content(backStackEntry: NavBackStackEntry, navigator: Navigator) {
        val vm = viewModel { viewModelFactory() }
        val state by vm.uiState.collectAsState()
        val toastHost = LocalToastHostState.current

        LaunchedEffect(vm) {
            vm.uiEvents.collect { event ->
                when (event) {
                    is ActiveBuildsUiEvent.NavigateToLogs ->
                        navigator.navigateTo(BuildLogsDestination.routeFor(event.buildId), false)

                    ActiveBuildsUiEvent.CancelFailed      -> launch {
                        toastHost.show(
                            ToastData(
                                type = ToastType.Error,
                                message = TextResource.ResText(Res.string.error_cancel_build),
                                duration = ToastDuration.Timed(),
                            ),
                        )
                    }
                }
            }
        }

        val connection by vm.connection.collectAsState()
        val scope = rememberCoroutineScope()
        val copied = TextResource.ResText(Res.string.handoff_copied)
        val onIntent = remember(vm) { { intent: ActiveBuildsIntent -> vm.onIntent(intent) } }
        ActiveBuildsContent(
            state = state,
            connection = connection,
            onIntent = onIntent,
            onCopied = {
                scope.launch {
                    toastHost.show(ToastData(type = ToastType.Success, message = copied, duration = ToastDuration.Timed()))
                }
            },
        )
    }
}
