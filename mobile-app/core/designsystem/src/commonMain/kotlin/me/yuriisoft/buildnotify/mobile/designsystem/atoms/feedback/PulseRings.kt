package me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Radar-style rings expanding from the centre, drawn behind [content].
 * Replaces the old pulsing ring progress on the discovery screens.
 */
@Composable
fun PulseRings(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.running.main,
    rings: Int = DEFAULT_RINGS,
    content: @Composable () -> Unit = {},
) {
    val size = GraphiteTheme.dimens.visual.stage
    val stroke = GraphiteTheme.dimens.visual.ringStroke
    val loops = GraphiteTheme.motion.loops
    val ringAlpha = GraphiteTheme.opacity.pulseRing
    val transition = rememberInfiniteTransition(label = "pulse")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(loops.pulseMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val maxRadius = this.size.minDimension / 2
            repeat(rings) { i ->
                val t = (phase + i.toFloat() / rings) % 1f
                drawCircle(
                    color = color.copy(alpha = (1f - t) * ringAlpha),
                    radius = maxRadius * (loops.pulseInnerRadius + (1f - loops.pulseInnerRadius) * t),
                    style = Stroke(width = stroke.toPx()),
                )
            }
        }
        content()
    }
}

private const val DEFAULT_RINGS = 3
