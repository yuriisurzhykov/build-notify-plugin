package me.yuriisoft.buildnotify.mobile.feature.buildstatus.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import me.tatarka.inject.annotations.Inject
import me.yuriisoft.buildnotify.mobile.core.navigation.Destination
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen
import me.yuriisoft.buildnotify.mobile.core.navigation.routes.BuildStatusDestination
import me.yuriisoft.buildnotify.mobile.feature.buildstatus.ui.BuildStatusContent

@Inject
@Immutable
class BuildStatusScreen(
    private val viewModelFactory: () -> BuildStatusViewModel,
) : Screen() {

    override val destination: Destination = BuildStatusDestination

    @Composable
    override fun Content(
        backStackEntry: NavBackStackEntry,
        navigator: Navigator
    ) {
        BuildStatusContent(

        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}