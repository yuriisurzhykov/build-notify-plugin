package me.yuriisoft.buildnotify.mobile.designsystem.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** Shared timing so every screen moves with the same rhythm. */
object GraphiteMotion {
    const val Fast = 150
    const val Medium = 280
    const val Slow = 450
    /** Delay between items of a staggered reveal. */
    const val Stagger = 55

    fun <T> enter(delay: Int = 0): AnimationSpec<T> = tween(Medium, delayMillis = delay, easing = FastOutSlowInEasing)
    fun <T> bouncy(): AnimationSpec<T> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    fun <T> settle(): AnimationSpec<T> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
}

/**
 * Fades and lifts [content] in once, after `index × Stagger` ms. Wrap the elements of a
 * screen in increasing indices to get a cascading entrance.
 */
@Composable
fun Reveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    val delay = index * GraphiteMotion.Stagger
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(GraphiteMotion.enter(delay)) +
            slideInVertically(GraphiteMotion.enter<IntOffset>(delay)) { it / 3 },
    ) {
        content()
    }
}

/** Springs [content] from 60 % scale on first composition — for success and status badges. */
@Composable
fun PopIn(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(tween(GraphiteMotion.Fast, delayMillis)) +
            scaleIn(GraphiteMotion.bouncy(), initialScale = 0.6f),
    ) {
        content()
    }
}

/** Horizontal shake whenever [trigger] changes — attention for failures and rejections. */
@Composable
fun Modifier.shakeOn(trigger: Any?): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        offset.snapTo(0f)
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 420
                -14f at 60
                12f at 130
                -8f at 200
                5f at 270
                -2f at 340
            },
        )
    }
    return graphicsLayer { translationX = offset.value * density }
}

/**
 * Radar-style rings expanding from the centre, drawn behind [content].
 * Replaces the old pulsing ring progress on the discovery screens.
 */
@Composable
fun PulseRings(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.running.main,
    size: Dp = 168.dp,
    rings: Int = 3,
    content: @Composable () -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val maxRadius = this.size.minDimension / 2
            repeat(rings) { i ->
                val t = (phase + i.toFloat() / rings) % 1f
                drawCircle(
                    color = color.copy(alpha = (1f - t) * 0.35f),
                    radius = maxRadius * (0.35f + 0.65f * t),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        content()
    }
}

/** Three dots bouncing in sequence — "still working". */
@Composable
fun BouncingDots(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.contentSecondary,
    dotSize: Dp = 6.dp,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(dotSize), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = 1_200
                        0f at (i * 150)
                        -1f at (i * 150 + 220) using FastOutSlowInEasing
                        0f at (i * 150 + 440)
                    },
                ),
                label = "dot$i",
            )
            Canvas(
                Modifier
                    .size(dotSize)
                    .graphicsLayer { translationY = lift * dotSize.toPx() },
            ) {
                drawCircle(color.copy(alpha = 0.55f + 0.45f * -lift))
            }
        }
    }
}

/** Gentle "breathing" scale for an element that waits on the user (e.g. the PIN). */
@Composable
fun Modifier.breathing(enabled: Boolean = true): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
