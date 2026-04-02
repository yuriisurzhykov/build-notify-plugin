package me.yuriisoft.buildnotify.mobile.toast.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.ui.theme.color.semantic.StatusRole
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Co-prime cycle durations (ms) prevent the three phase waves
 * from visually "locking" into a repeating pattern.
 */
private const val PHASE_A_MS = 7_000
private const val PHASE_B_MS = 5_300
private const val PHASE_C_MS = 3_700

private const val TWO_PI = (2.0 * PI).toFloat()
private const val NANOS_PER_MS = 1_000_000L

/**
 * Infinitely animated [Brush] whose colour-stop positions drift
 * inside a linear gradient built from [statusRole] colours.
 *
 * Three sine waves with co-prime periods modulate stop positions,
 * producing organic motion that avoids visible repetition.
 *
 * For a richer multi-layer aurora effect, see [auroraBackground].
 */
@Composable
fun auroraGradientBrush(statusRole: StatusRole): Brush {
    val phases = rememberAuroraPhases()
    return buildLinearAurora(statusRole, phases)
}

/**
 * Draws three overlapping gradient layers behind the content:
 *
 * 1. **Base sweep** — a slow-rotating linear gradient spanning all three colours.
 * 2. **Focal glow** — a radial gradient of [StatusRole.main] orbiting the banner.
 * 3. **Accent shimmer** — a smaller [StatusRole.onContainer] radial counter-orbiting.
 *
 * Each layer is phase-shifted so the combined result is a soft,
 * continuously shifting aurora.
 *
 * Implemented as a [ModifierNodeElement]-backed [DrawModifierNode],
 * avoiding the overhead and skippability issues of a composed modifier.
 */
fun Modifier.auroraBackground(statusRole: StatusRole): Modifier =
    this then AuroraBackgroundElement(statusRole)

private data class AuroraBackgroundElement(
    private val statusRole: StatusRole,
) : ModifierNodeElement<AuroraBackgroundNode>() {

    override fun create(): AuroraBackgroundNode = AuroraBackgroundNode(statusRole)

    override fun update(node: AuroraBackgroundNode) {
        node.update(statusRole)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "auroraBackground"
        properties["statusRole"] = statusRole
    }
}

/**
 * [DrawModifierNode] that renders the aurora gradient and drives
 * its own frame-synchronised animation loop via [withFrameNanos].
 *
 * Phase values are derived from absolute frame time using modular
 * arithmetic, so the animation is seamless across attach/detach
 * cycles and never accumulates floating-point drift.
 */
private class AuroraBackgroundNode(
    private var statusRole: StatusRole,
) : Modifier.Node(), DrawModifierNode {

    private var phaseA = 0f
    private var phaseB = 0f
    private var phaseC = 0f

    fun update(newRole: StatusRole) {
        if (statusRole != newRole) {
            statusRole = newRole
            invalidateDraw()
        }
    }

    override fun onAttach() {
        coroutineScope.launch {
            while (true) {
                withFrameNanos { nanos ->
                    val ms = nanos / NANOS_PER_MS
                    phaseA = (ms % PHASE_A_MS).toFloat() / PHASE_A_MS * TWO_PI
                    phaseB = (ms % PHASE_B_MS).toFloat() / PHASE_B_MS * TWO_PI
                    phaseC = (ms % PHASE_C_MS).toFloat() / PHASE_C_MS * TWO_PI
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val w = size.width
        val h = size.height
        val diagonal = sqrt(w * w + h * h)

        val main = statusRole.main
        val container = statusRole.container
        val onContainer = statusRole.onContainer
        val glow = lerp(main, container, 0.5f)

        val angle = (35f + 25f * sin(phaseA)) * PI.toFloat() / 180f
        val halfDiag = diagonal / 2f
        val cx = w / 2f
        val cy = h / 2f
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(main, glow, container),
                start = Offset(cx - cos(angle) * halfDiag, cy - sin(angle) * halfDiag),
                end = Offset(cx + cos(angle) * halfDiag, cy + sin(angle) * halfDiag),
            ),
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(main.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(
                    x = w * (0.35f + 0.25f * sin(phaseB)),
                    y = h * (0.40f + 0.20f * cos(phaseB)),
                ),
                radius = diagonal * 0.55f,
            ),
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(onContainer.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(
                    x = w * (0.65f + 0.20f * cos(phaseC)),
                    y = h * (0.55f + 0.25f * sin(phaseC)),
                ),
                radius = diagonal * 0.45f,
            ),
        )

        drawContent()
    }
}

@Stable
private data class AuroraPhases(val a: Float, val b: Float, val c: Float)

@Composable
private fun rememberAuroraPhases(): AuroraPhases {
    val transition = rememberInfiniteTransition(label = "aurora")

    val a by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            tween(PHASE_A_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_a",
    )
    val b by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            tween(PHASE_B_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_b",
    )
    val c by transition.animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(
            tween(PHASE_C_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "aurora_c",
    )

    return AuroraPhases(a, b, c)
}

/**
 * Builds a single [linearGradient][Brush.linearGradient] whose
 * colour-stop positions are driven by three running [AuroraPhases].
 *
 * Stops drift inside non-overlapping bands so they stay
 * monotonically ordered without runtime sorting.
 */
private fun buildLinearAurora(role: StatusRole, p: AuroraPhases): Brush {
    val s1 = (0.00f + 0.10f * sin(p.a)).coerceAtLeast(0f)
    val s2 = (0.33f + 0.08f * sin(p.b)).coerceIn(s1 + 0.01f, 0.50f)
    val s3 = (0.60f + 0.08f * sin(p.c)).coerceIn(s2 + 0.01f, 0.80f)
    val s4 = (0.90f + 0.08f * cos(p.a + p.b)).coerceIn(s3 + 0.01f, 1.0f)

    val midFraction = (0.5f + 0.15f * sin(p.b + p.c)).coerceIn(0f, 1f)
    val mid = lerp(role.main, role.container, midFraction)

    return Brush.linearGradient(
        colorStops = arrayOf(
            s1 to role.main,
            s2 to mid,
            s3 to role.container,
            s4 to role.onContainer,
        ),
    )
}
