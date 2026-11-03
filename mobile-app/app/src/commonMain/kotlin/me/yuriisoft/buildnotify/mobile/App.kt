package me.yuriisoft.buildnotify.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.core.navigation.StartRoute
import me.yuriisoft.buildnotify.mobile.feature.networkstatus.NetworkStatusEffect
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.toast.LocalToastHostState
import me.yuriisoft.buildnotify.mobile.toast.ToastHostState
import me.yuriisoft.buildnotify.mobile.toast.ui.ToastHost
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
fun App(
    screens: Set<Screen>,
    startRoute: StartRoute,
    connectionState: StateFlow<ConnectionState>,
    /** Routes requested from outside the UI, e.g. a tapped build notification. */
    externalRoutes: Flow<String> = emptyFlow(),
) {
    // Graphite renders every screen. BuildNotifyTheme only backs the legacy ui-catalog.
    BuildNotifyTheme {
        GraphiteTheme {
            val toastHostState = remember { ToastHostState() }
            val navController = rememberNavController()
            val navigator = remember(navController) { AppNavigator(navController) }

            CompositionLocalProvider(LocalToastHostState provides toastHostState) {
                Box(Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        navigator = navigator,
                        screens = screens,
                        startRoute = remember { startRoute.resolve().route },
                    )
                    ToastHost(
                        hostState = toastHostState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
                NetworkStatusEffect(connectionState, toastHostState, navigator)
                LaunchedEffect(navigator, externalRoutes) {
                    externalRoutes.collect { route -> navigator.navigateTo(route, false) }
                }
            }
        }
    }
}
