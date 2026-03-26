package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.ring

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import me.yuriisoft.buildnotify.mobile.ui.theme.brush.semantic.GradientSpec

@Stable
interface RingProgressMode {
    val rangeInfo: ProgressBarRangeInfo

    @Composable
    fun rememberDrawer(): RingProgressDrawer

    @Immutable
    data class Pulsing(
        val spec: PulsingRingSpec = PulsingRingSpec(),
    ) : RingProgressMode {

        override val rangeInfo: ProgressBarRangeInfo
            get() = ProgressBarRangeInfo.Indeterminate

        @Composable
        override fun rememberDrawer(): RingProgressDrawer {
            val transition = rememberInfiniteTransition()
            val scales = List(spec.ringCount) { index ->
                transition.animateFloat(
                    initialValue = spec.minScale,
                    targetValue = spec.maxScale,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = spec.durationMillis,
                            delayMillis = index * spec.staggerDelayMillis,
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Restart,
                    ),
                )
            }
            val alphas = List(spec.ringCount) { index ->
                transition.animateFloat(
                    initialValue = spec.maxAlpha,
                    targetValue = spec.minAlpha,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = spec.durationMillis,
                            delayMillis = index * spec.staggerDelayMillis,
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Restart,
                    ),
                )
            }
            return remember(this) {
                RingProgressDrawer { gradient ->
                    drawPulsingRings(gradient, scales, alphas)
                }
            }
        }
    }
}

private fun DrawScope.drawPulsingRings(
    gradient: GradientSpec,
    scales: List<State<Float>>,
    alphas: List<State<Float>>,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = minOf(size.width, size.height) / 2f

    for (i in scales.indices) {
        val radius = baseRadius * scales[i].value
        drawCircle(
            brush = gradient.toBrush(size),
            radius = radius,
            center = center,
            alpha = alphas[i].value,
        )
    }
}
