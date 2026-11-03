package me.yuriisoft.buildnotify.mobile.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteMotion

typealias EnterSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
typealias ExitSpec = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?

fun ScreenTransitions.enterSpec(motion: GraphiteMotion): EnterSpec? = when (this) {
    is ScreenTransitions.None            -> null
    is ScreenTransitions.SlideHorizontal -> enter { slideInHorizontally { it } }
    is ScreenTransitions.Fade            -> enter { fadeIn(motion.enter()) }
}

fun ScreenTransitions.exitSpec(motion: GraphiteMotion): ExitSpec? = when (this) {
    is ScreenTransitions.None            -> null
    is ScreenTransitions.SlideHorizontal -> exit { slideOutHorizontally { -it } }
    is ScreenTransitions.Fade            -> exit { fadeOut(motion.fast()) }
}

fun ScreenTransitions.popEnterSpec(motion: GraphiteMotion): EnterSpec? = when (this) {
    is ScreenTransitions.None            -> null
    is ScreenTransitions.SlideHorizontal -> enter { slideInHorizontally { -it } }
    is ScreenTransitions.Fade            -> enter { fadeIn(motion.enter()) }
}

fun ScreenTransitions.popExitSpec(motion: GraphiteMotion): ExitSpec? = when (this) {
    is ScreenTransitions.None            -> null
    is ScreenTransitions.SlideHorizontal -> exit { slideOutHorizontally { it } }
    is ScreenTransitions.Fade            -> exit { fadeOut(motion.fast()) }
}

/** Gives a lambda its navigation-transition type, so the branches above infer cleanly. */
private fun enter(spec: EnterSpec): EnterSpec = spec

private fun exit(spec: ExitSpec): ExitSpec = spec
