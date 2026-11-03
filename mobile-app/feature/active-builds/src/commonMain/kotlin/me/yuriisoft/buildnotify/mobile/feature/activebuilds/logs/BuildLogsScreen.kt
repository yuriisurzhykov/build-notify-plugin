package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_copied
import me.yuriisoft.buildnotify.mobile.toast.LocalToastHostState
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import me.tatarka.inject.annotations.Inject
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.core.navigation.ScreenTransitions
import me.yuriisoft.buildnotify.mobile.core.navigation.routes.BuildLogsDestination
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui.BuildLogsContent

@Inject
@Immutable
class BuildLogsScreen(
    private val repository: IActiveBuildRepository,
) : Screen() {

    override val destination = BuildLogsDestination
    override val transitions = ScreenTransitions.SlideHorizontal

    @Composable
    override fun Content(backStackEntry: NavBackStackEntry, navigator: Navigator) {
        val buildId = remember(backStackEntry) {
            backStackEntry.savedStateHandle.get<String>(BuildLogsDestination.ARG_BUILD_ID).orEmpty()
        }
        val vm = viewModel(key = "build_logs_$buildId") {
            BuildLogsViewModel(buildId = buildId, repository = repository)
        }
        val state by vm.uiState.collectAsState()

        val onIntent = remember(vm) { { intent: BuildLogsIntent -> vm.onIntent(intent) } }
        val toastHost = LocalToastHostState.current
        val scope = rememberCoroutineScope()
        val copied = TextResource.ResText(Res.string.logs_copied)
        BuildLogsContent(
            state = state,
            onIntent = onIntent,
            onBack = navigator::back,
            onCopied = {
                scope.launch {
                    toastHost.show(
                        ToastData(type = ToastType.Success, message = copied, duration = ToastDuration.Timed()),
                    )
                }
            },
        )
    }
}
