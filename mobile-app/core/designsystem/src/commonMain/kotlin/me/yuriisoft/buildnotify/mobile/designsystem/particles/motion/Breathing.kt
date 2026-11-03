package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Gentle "breathing" scale for an element that waits on the user (e.g. the PIN). */
@Composable
fun Modifier.breathing(enabled: Boolean = true): Modifier {
    if (!enabled) return this
    val motion = GraphiteTheme.motion
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = motion.scales.breathing,
        animationSpec = infiniteRepeatable(
            tween(motion.loops.breathingMillis, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "scale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
