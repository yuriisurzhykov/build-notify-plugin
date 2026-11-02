package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

internal const val DisabledAlpha = 0.38f
private const val PressedScale = 0.97f
private const val PressedAlpha = 0.85f

/**
 * Graphite press feedback, identical on every platform (no Material ripple):
 * the element shrinks slightly and dims while pressed.
 */
@Composable
internal fun Modifier.pressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role = Role.Button,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) PressedScale else 1f, tween(durationMillis = 120))
    val alpha = when {
        !enabled -> DisabledAlpha
        pressed  -> PressedAlpha
        else     -> 1f
    }
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}
