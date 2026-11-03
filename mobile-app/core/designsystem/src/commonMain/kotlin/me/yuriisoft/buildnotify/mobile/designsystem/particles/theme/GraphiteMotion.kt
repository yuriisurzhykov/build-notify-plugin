package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * Motion tokens: one rhythm for every screen. Read through [GraphiteTheme.motion];
 * components never write a duration, delay or scale literal.
 */
@Immutable
data class GraphiteMotion(
    val fastMillis: Int = 150,
    val mediumMillis: Int = 280,
    val slowMillis: Int = 450,
    /** Delay between items of a staggered reveal. */
    val staggerMillis: Int = 55,
    val scales: Scales = Scales(),
    val loops: Loops = Loops(),
    val shake: Shake = Shake(),
) {
    /** Entrance of content; [delayMillis] staggers a sequence. */
    fun <T> enter(delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(mediumMillis, delayMillis, FastOutSlowInEasing)

    fun <T> fast(delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(fastMillis, delayMillis)

    fun <T> slow(): FiniteAnimationSpec<T> = tween(slowMillis)

    /** Overshooting spring for things that pop into place. */
    fun <T> bouncy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Critically damped spring for sizes and positions that settle. */
    fun <T> settle(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Start or end values of scale-based transitions. */
    @Immutable
    data class Scales(
        val pressed: Float = 0.97f,
        val morph: Float = 0.96f,
        val popIn: Float = 0.6f,
        val appearPop: Float = 0.8f,
        val breathing: Float = 1.03f,
        /** Part of its height that revealed content rises from. */
        val revealLift: Float = 1f / 3,
        /** Part of its height that a replaced line rises from. */
        val riseLift: Float = 0.5f,
    )

    /** Periods of the endless animations. */
    @Immutable
    data class Loops(
        val breathingMillis: Int = 1_600,
        val pulseMillis: Int = 2_400,
        /** Innermost ring radius, as a part of the outer radius. */
        val pulseInnerRadius: Float = 0.35f,
        val progressMillis: Int = 1_400,
        /** Width of the indeterminate progress segment, as a part of the track. */
        val progressSegment: Float = 0.3f,
        val dotsCycleMillis: Int = 1_200,
        /** Offset between the dots of one cycle. */
        val dotsStepMillis: Int = 150,
        val dotsPeakMillis: Int = 220,
        val dotsLandMillis: Int = 440,
    )

    /** A decaying horizontal shake: offsets in dp at the given times. */
    @Immutable
    data class Shake(
        val durationMillis: Int = 420,
        val keyframes: List<Pair<Float, Int>> = listOf(-14f to 60, 12f to 130, -8f to 200, 5f to 270, -2f to 340),
    )
}
