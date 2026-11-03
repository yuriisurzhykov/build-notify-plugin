package me.yuriisoft.buildnotify.mobile.designsystem.motion

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

/*
 * The Graphite catalogue of content transitions. Features pass one of these to [Swap]
 * instead of composing enter/exit specs themselves, so every screen moves alike.
 */

/** Plain cross-fade between two contents. */
fun <S> AnimatedContentTransitionScope<S>.crossfade(): ContentTransform =
    fadeIn(GraphiteMotion.enter()) togetherWith fadeOut(GraphiteMotion.fast())

/** The new content grows slightly into place while the size follows — for hero states. */
fun <S> AnimatedContentTransitionScope<S>.morph(): ContentTransform =
    (fadeIn(GraphiteMotion.enter()) + scaleIn(GraphiteMotion.enter(), initialScale = MORPH_SCALE))
        .togetherWith(fadeOut(GraphiteMotion.fast()))
        .using(SizeTransform(clip = false))

/** Split-flap roll: the new value drops in from above, the old one falls out below. */
fun <S> AnimatedContentTransitionScope<S>.roll(): ContentTransform =
    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())

/** The new value rises from below — for a line of text that is replaced (current task). */
fun <S> AnimatedContentTransitionScope<S>.rise(): ContentTransform =
    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())

/**
 * Step navigation inside one screen: a higher [targetOrder] slides in from the right,
 * a lower one from the left; the same step — or a negative order (an out-of-band state
 * such as "no network") — cross-fades.
 *
 * Branch order matters and is part of the contract: a negative target that is lower than
 * the current step still slides backwards.
 */
fun <S> AnimatedContentTransitionScope<S>.directional(initialOrder: Int, targetOrder: Int): ContentTransform {
    val isForward = targetOrder > initialOrder
    val isBackward = targetOrder < initialOrder
    return when {
        isForward  -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        isBackward -> slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        else       -> fadeIn() togetherWith fadeOut()
    }
}

private const val MORPH_SCALE = 0.96f
