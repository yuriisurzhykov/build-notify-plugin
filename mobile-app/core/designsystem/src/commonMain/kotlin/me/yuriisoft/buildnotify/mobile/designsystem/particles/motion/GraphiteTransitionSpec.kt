package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteMotion

/** The animation of [transition] with the timing of [motion]. */
internal fun <S> AnimatedContentTransitionScope<S>.contentTransform(
    transition: GraphiteTransition<S>,
    motion: GraphiteMotion,
): ContentTransform = when (transition) {
    is GraphiteTransition.Crossfade   -> crossfade(motion)
    is GraphiteTransition.Morph       ->
        (fadeIn(motion.enter()) + scaleIn(motion.enter(), initialScale = motion.scales.morph))
            .togetherWith(fadeOut(motion.fast()))
            .using(SizeTransform(clip = false))

    is GraphiteTransition.Roll        ->
        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())

    is GraphiteTransition.Rise        -> rise(motion.scales.riseLift)
    is GraphiteTransition.Directional ->
        directional(initialOrder = transition.order(initialState), targetOrder = transition.order(targetState))
}

private fun crossfade(motion: GraphiteMotion): ContentTransform =
    fadeIn(motion.enter()) togetherWith fadeOut(motion.fast())

private fun rise(lift: Float): ContentTransform =
    (slideInVertically { (it * lift).toInt() } + fadeIn()) togetherWith
        (slideOutVertically { -(it * lift).toInt() } + fadeOut())

/** Branch order is part of the contract, see [GraphiteTransition.Directional]. */
private fun directional(initialOrder: Int, targetOrder: Int): ContentTransform = when {
    targetOrder > initialOrder -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    targetOrder < initialOrder -> slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
    else                       -> fadeIn() togetherWith fadeOut()
}
