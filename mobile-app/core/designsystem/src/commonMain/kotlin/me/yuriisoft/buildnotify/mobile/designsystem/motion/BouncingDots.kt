package me.yuriisoft.buildnotify.mobile.designsystem.motion

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** Three dots bouncing in sequence — "still working". */
@Composable
fun BouncingDots(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.contentSecondary,
    dotSize: Dp = 6.dp,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
