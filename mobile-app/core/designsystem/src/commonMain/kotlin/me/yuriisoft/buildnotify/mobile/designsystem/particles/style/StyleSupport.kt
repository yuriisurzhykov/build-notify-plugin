package me.yuriisoft.buildnotify.mobile.designsystem.particles.style

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.scale
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** The Graphite reaction to touch, identical on every platform (no Material ripple). */
@Composable
internal fun rememberPressFeedback(): Style {
    val motion = GraphiteTheme.motion
    val opacity = GraphiteTheme.opacity
    return remember(motion, opacity) {
        Style {
            pressed {
                animate(motion.fast()) {
                    scale(motion.scales.pressed)
                    alpha(opacity.pressed)
                }
            }
            disabled {
                alpha(opacity.disabled)
            }
        }
    }
}

/** Applies a non-interactive [style]. */
@Composable
internal fun Modifier.styled(style: Style): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val state = rememberUpdatedStyleState(interaction) { }
    return styleable(state, style)
}

/**
 * Makes the element clickable and drives [style] from its interaction state:
 * pressed/disabled come from [rememberPressFeedback]; [selected] and [checked] let the style
 * declare `selected { }` / `checked { }` variants.
 */
@Composable
internal fun Modifier.styledClickable(
    style: Style,
    onClick: () -> Unit,
    enabled: Boolean = true,
    selected: Boolean = false,
    checked: Boolean = false,
    role: Role = Role.Button,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val feedback = rememberPressFeedback()
    val state = rememberUpdatedStyleState(interaction) {
        it.isEnabled = enabled
        it.isSelected = selected
        it.isChecked = checked
    }
    return this
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
        .styleable(state, feedback.then(style))
}

/** Applies [style] with its `checked { }` variant driven by [checked]; not clickable itself. */
@Composable
internal fun Modifier.styledToggle(style: Style, checked: Boolean): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val state = rememberUpdatedStyleState(interaction) { it.isChecked = checked }
    return styleable(state, style)
}

/*
 * Graphite components describe their look as Compose Styles: one Style per component
 * holds background, shape, border, padding, size and the reaction to interaction states.
 * These helpers bind a Style to an element; components never stack raw modifiers for
 * visuals.
 */
