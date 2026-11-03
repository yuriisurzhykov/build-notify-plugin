package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Shared timing so every screen moves with the same rhythm. */
object GraphiteMotion {
    const val Fast = 150
    const val Medium = 280
    const val Slow = 450
    /** Delay between items of a staggered reveal. */
    const val Stagger = 55

    fun <T> enter(delay: Int = 0): FiniteAnimationSpec<T> = tween(
        Medium,
        delayMillis = delay,
        easing = FastOutSlowInEasing,
    )
    fun <T> bouncy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    fun <T> settle(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    fun <T> fast(): FiniteAnimationSpec<T> = tween(Fast)
    fun <T> slow(): FiniteAnimationSpec<T> = tween(Slow)
}
