package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** The one place that knows what each [ButtonVariant] and [ButtonSize] looks like. */
@Composable
internal fun rememberButtonAppearance(variant: ButtonVariant, size: ButtonSize): ButtonAppearance {
    val colors = GraphiteTheme.colors
    val controlShape = GraphiteTheme.shapes.control
    val sizes = GraphiteTheme.sizes
    val padding = GraphiteTheme.spacing.l
    val height = if (size == ButtonSize.Large) sizes.buttonLarge else sizes.buttonRegular

    val (container, content, outline) = when (variant) {
        ButtonVariant.Primary   -> Triple(colors.inverse, colors.onInverse, Color.Transparent)
        ButtonVariant.Secondary -> Triple(colors.surfaceRaised, colors.content, Color.Transparent)
        ButtonVariant.Outline   -> Triple(Color.Transparent, colors.content, colors.lineStrong)
        ButtonVariant.Danger    -> Triple(
            Color.Transparent,
            colors.failure.main,
            colors.failure.main.copy(alpha = 0.4f),
        )
    }
    return remember(container, content, outline, controlShape, height, padding) {
        ButtonAppearance(
            style = Style {
                shape(controlShape)
                background(container)
                border(sizes.hairline, outline)
                minHeight(height)
                contentPaddingHorizontal(padding)
            },
            content = content,
        )
    }
}

/** Round, icon-only button container with a full touch target. */
@Composable
internal fun rememberIconButtonStyle(): Style {
    val target = GraphiteTheme.sizes.touchTarget
    val pill = GraphiteTheme.shapes.pill
    return remember(target, pill) {
        Style {
            shape(pill)
            size(target)
        }
    }
}
