package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.dot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import me.yuriisoft.buildnotify.mobile.ui.components.progress.ProgressDefaults

@Stable
interface DotProgressMode {
    val rangeInfo: ProgressBarRangeInfo

    @Composable
    fun rememberDrawer(): DotProgressDrawer

    @Immutable
    data class Bouncing(
        val spec: BouncingDotSpec = BouncingDotSpec(),
    ) : DotProgressMode {

        override val rangeInfo: ProgressBarRangeInfo
            get() = ProgressBarRangeInfo.Indeterminate

        @Composable
        override fun rememberDrawer(): DotProgressDrawer {
            val transition = rememberInfiniteTransition()
            val offsets = (0 until ProgressDefaults.DotCount).map { index ->
                transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = spec.durationMillis,
                            delayMillis = index * spec.staggerDelayMillis,
                            easing = spec.easing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
            return remember(this) {
                DotProgressDrawer { color, dotCount, dotRadius, spacing ->
                    drawBouncingDots(color, dotCount, dotRadius, spacing, offsets.map { it.value })
                }
            }
        }
    }
}

private fun DrawScope.drawBouncingDots(
    color: Color,
    dotCount: Int,
    dotRadius: Float,
    spacing: Float,
    offsets: List<Float>,
) {
    val totalWidth = dotCount * dotRadius * 2f + (dotCount - 1) * spacing
    val startX = (size.width - totalWidth) / 2f + dotRadius
    val centerY = size.height / 2f
    val bounceHeight = size.height * ProgressDefaults.DotBounceRatio

    for (i in 0 until dotCount) {
        val x = startX + i * (dotRadius * 2f + spacing)
        val fraction = offsets.getOrElse(i) { 0f }
        val y = centerY - fraction * bounceHeight
        drawCircle(color = color, radius = dotRadius, center = Offset(x, y))
    }
}
