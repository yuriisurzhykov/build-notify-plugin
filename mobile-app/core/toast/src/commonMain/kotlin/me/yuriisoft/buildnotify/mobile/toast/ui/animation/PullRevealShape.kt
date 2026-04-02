package me.yuriisoft.buildnotify.mobile.toast.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Clip shape producing a "pull-reveal" effect from the top edge.
 *
 * The bottom boundary is a cubic Bézier whose center drops ahead of
 * the corners, giving the appearance of content being pulled down
 * from the middle.
 *
 * When [centerProgress] equals [edgeProgress] the Bézier degenerates
 * to a horizontal line, yielding a plain rectangle.
 *
 * @param centerProgress 0..1 — how far the center of the bottom edge
 *   has dropped toward full banner height.
 * @param edgeProgress   0..1 — how far the left/right corners of
 *   the bottom edge have dropped.
 */
@Stable
class PullRevealShape(
    private val centerProgress: Float,
    private val edgeProgress: Float,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val centerDrop = h * centerProgress
        val edgeDrop = h * edgeProgress

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, edgeDrop)
            cubicTo(
                x1 = w * 0.75f, y1 = centerDrop,
                x2 = w * 0.25f, y2 = centerDrop,
                x3 = 0f, y3 = edgeDrop,
            )
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PullRevealShape) return false
        return centerProgress == other.centerProgress &&
            edgeProgress == other.edgeProgress
    }

    override fun hashCode(): Int {
        var result = centerProgress.hashCode()
        result = 31 * result + edgeProgress.hashCode()
        return result
    }
}

/**
 * Animatable state that drives a [PullRevealShape] with staggered
 * center/edge timing.
 *
 * - **Entrance**: center drops first (400 ms), edges follow 150 ms later (350 ms).
 * - **Exit**: edges retract first (350 ms), center follows 150 ms later (400 ms).
 */
@Stable
class PullRevealState internal constructor() {

    private val center = Animatable(0f)
    private val edge = Animatable(0f)

    val shape: Shape by derivedStateOf {
        PullRevealShape(
            centerProgress = center.value,
            edgeProgress = edge.value,
        )
    }

    val isFullyRevealed: Boolean by derivedStateOf {
        center.value == 1f && edge.value == 1f
    }

    val isFullyHidden: Boolean by derivedStateOf {
        center.value == 0f && edge.value == 0f
    }

    suspend fun enter() = coroutineScope {
        launch { center.animateTo(1f, enterCenterSpec) }
        launch { edge.animateTo(1f, enterEdgeSpec) }
    }

    suspend fun exit() = coroutineScope {
        launch { edge.animateTo(0f, exitEdgeSpec) }
        launch { center.animateTo(0f, exitCenterSpec) }
    }

    suspend fun snapTo(visible: Boolean) {
        val target = if (visible) 1f else 0f
        center.snapTo(target)
        edge.snapTo(target)
    }
}

@Composable
fun rememberPullRevealState(): PullRevealState = remember { PullRevealState() }

private const val CENTER_DURATION_MS = 400
private const val EDGE_DURATION_MS = 350
private const val STAGGER_DELAY_MS = 150

private val enterCenterSpec: AnimationSpec<Float> = tween(
    durationMillis = CENTER_DURATION_MS,
    easing = FastOutSlowInEasing,
)

private val enterEdgeSpec: AnimationSpec<Float> = tween(
    durationMillis = EDGE_DURATION_MS,
    delayMillis = STAGGER_DELAY_MS,
    easing = FastOutSlowInEasing,
)

private val exitEdgeSpec: AnimationSpec<Float> = tween(
    durationMillis = EDGE_DURATION_MS,
    easing = FastOutSlowInEasing,
)

private val exitCenterSpec: AnimationSpec<Float> = tween(
    durationMillis = CENTER_DURATION_MS,
    delayMillis = STAGGER_DELAY_MS,
    easing = FastOutSlowInEasing,
)
