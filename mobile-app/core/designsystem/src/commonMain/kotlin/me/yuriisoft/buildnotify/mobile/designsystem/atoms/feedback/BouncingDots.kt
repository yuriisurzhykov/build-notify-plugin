package me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Three dots bouncing in sequence — "still working". */
@Composable
fun BouncingDots(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.contentSecondary,
) {
    val dotSize = GraphiteTheme.dimens.visual.bouncingDot
    val loops = GraphiteTheme.motion.loops
    val restingAlpha = GraphiteTheme.opacity.restingDot
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOTS) { i ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        val start = i * loops.dotsStepMillis
                        durationMillis = loops.dotsCycleMillis
                        0f at start
                        -1f at (start + loops.dotsPeakMillis) using FastOutSlowInEasing
                        0f at (start + loops.dotsLandMillis)
                    },
                ),
                label = "dot$i",
            )
            Canvas(
                Modifier
                    .size(dotSize)
                    .graphicsLayer { translationY = lift * dotSize.toPx() },
            ) {
                drawCircle(color.copy(alpha = restingAlpha + (1f - restingAlpha) * -lift))
            }
        }
    }
}

private const val DOTS = 3
