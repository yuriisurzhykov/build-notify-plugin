package me.yuriisoft.buildnotify.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.core.navigation.enterSpec
import me.yuriisoft.buildnotify.mobile.core.navigation.exitSpec
import me.yuriisoft.buildnotify.mobile.core.navigation.popEnterSpec
import me.yuriisoft.buildnotify.mobile.core.navigation.popExitSpec
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.templates.screens.ScreenSurface

@Composable
fun AppNavGraph(
    navController: NavHostController,
    navigator: AppNavigator,
    screens: Set<Screen>,
    startRoute: String,
) {
    val motion = GraphiteTheme.motion
    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        screens.forEach { screen ->
            composable(
                route = screen.destination.route,
                arguments = screen.destination.arguments,
                deepLinks = screen.destination.deepLinks,
                enterTransition = screen.transitions.enterSpec(motion),
                exitTransition = screen.transitions.exitSpec(motion),
                popEnterTransition = screen.transitions.popEnterSpec(motion),
                popExitTransition = screen.transitions.popExitSpec(motion),
            ) { backStackEntry ->
                ScreenSurface(Modifier.fillMaxSize()) {
                    screen.Content(backStackEntry, navigator)
                }
            }
        }
    }
}
