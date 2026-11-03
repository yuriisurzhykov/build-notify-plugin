package me.yuriisoft.buildnotify.mobile.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.GraphiteMotion

data class ScreenTransitions(
    val enter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    val exit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    val popEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    val popExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
) {

    companion object {

        val None = ScreenTransitions()

        val SlideHorizontal = ScreenTransitions(
            enter = { slideInHorizontally { it } },
            exit = { slideOutHorizontally { -it } },
            popEnter = { slideInHorizontally { -it } },
            popExit = { slideOutHorizontally { it } },
        )

        val Fade = ScreenTransitions(
            enter = { fadeIn(GraphiteMotion.enter()) },
            exit = { fadeOut(GraphiteMotion.fast()) },
            popEnter = { fadeIn(GraphiteMotion.enter()) },
            popExit = { fadeOut(GraphiteMotion.fast()) },
        )
    }
}
