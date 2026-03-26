package me.yuriisoft.buildnotify.mobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.ui.components.layout.Surface
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
fun AppNavGraph(
    navController: NavHostController,
    screens: Set<Screen>,
    startRoute: String,
) {
    val navigator = remember(navController) { AppNavigator(navController) }

    NavHost(navController = navController, startDestination = startRoute) {
        screens.forEach { screen ->
            composable(
                route = screen.destination.route,
                arguments = screen.destination.arguments,
                deepLinks = screen.destination.deepLinks,
                enterTransition = screen.transitions.enter,
                exitTransition = screen.transitions.exit,
                popEnterTransition = screen.transitions.popEnter,
                popExitTransition = screen.transitions.popExit,
            ) { backStackEntry ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BuildNotifyTheme.colors.surface.background,
                    shape = BuildNotifyTheme.shapes.small,
                ) {
                    screen.Content(backStackEntry, navigator)
                }
            }
        }
    }
}
