package me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Thin progress track. `progress = null` animates an indeterminate segment —
 * Gradle does not report a total task count, so this is the default for builds.
 */
@Composable
fun ProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.running.main,
    trackColor: Color = GraphiteTheme.colors.content.copy(alpha = GraphiteTheme.opacity.progressTrack),
) {
    val loops = GraphiteTheme.motion.loops
    val segment = loops.progressSegment
    val transition = rememberInfiniteTransition(label = "progress")
    // Determinate bars ignore the phase; the transition is cheap and keeps the call stable.
    val phase by transition.animateFloat(
        initialValue = -segment,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(loops.progressMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    val range = if (progress == null) ProgressBarRangeInfo.Indeterminate
    else ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(GraphiteTheme.sizes.progressTrack)
            .semantics { progressBarRangeInfo = range },
    ) {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(trackColor, cornerRadius = radius)
        if (progress != null) {
            drawRoundRect(
                color,
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = radius,
            )
        } else {
            val start = (size.width * phase).coerceAtLeast(0f)
            val end = (size.width * (phase + segment)).coerceAtMost(size.width)
            if (end > start) {
                drawRoundRect(
                    color,
                    topLeft = Offset(start, 0f),
                    size = Size(end - start, size.height),
                    cornerRadius = radius,
                )
            }
        }
    }
}
