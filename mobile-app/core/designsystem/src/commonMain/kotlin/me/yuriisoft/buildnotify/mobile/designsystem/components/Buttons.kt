package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteContentColor

/**
 * - [Primary]   — the one action the screen exists for ("Передать агенту"). Max one per screen.
 * - [Secondary] — neutral filled action ("Лог").
 * - [Outline]   — neutral action on a tinted hero ("Перезапустить").
 * - [Danger]    — destructive, never filled ("Остановить").
 */
enum class ButtonVariant { Primary, Secondary, Outline, Danger }

enum class ButtonSize { Regular, Large }

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Regular,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = GraphiteTheme.colors
    val shape = GraphiteTheme.shapes.control
    val sizes = GraphiteTheme.sizes
    val hairline = sizes.hairline

    val (container, content, border) = when (variant) {
        ButtonVariant.Primary   -> Triple(colors.inverse, colors.onInverse, Color.Transparent)
        ButtonVariant.Secondary -> Triple(colors.surfaceRaised, colors.content, Color.Transparent)
        ButtonVariant.Outline   -> Triple(Color.Transparent, colors.content, colors.lineStrong)
        ButtonVariant.Danger    -> Triple(Color.Transparent, colors.failure.main, colors.failure.main.copy(alpha = 0.4f))
    }
    val height = if (size == ButtonSize.Large) sizes.buttonLarge else sizes.buttonRegular
    val textStyle = if (size == ButtonSize.Large) GraphiteTheme.typography.buttonLarge else GraphiteTheme.typography.button

    CompositionLocalProvider(LocalGraphiteContentColor provides content) {
        Row(
            modifier = modifier
                .heightIn(min = height)
                .pressable(onClick = onClick, enabled = enabled)
                .clip(shape)
                .background(container, shape)
                .border(hairline, border, shape)
                .padding(horizontal = GraphiteTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { Icon(it, contentDescription = null, size = sizes.icon) }
            Text(text = text, style = textStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailingIcon?.let { Icon(it, contentDescription = null, size = sizes.icon) }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) = Button(text, onClick, modifier, ButtonVariant.Primary, size, leadingIcon, trailingIcon, enabled)

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) = Button(text, onClick, modifier, ButtonVariant.Secondary, ButtonSize.Regular, leadingIcon, null, enabled)

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) = Button(text, onClick, modifier, ButtonVariant.Outline, ButtonSize.Regular, leadingIcon, null, enabled)

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) = Button(text, onClick, modifier, ButtonVariant.Danger, ButtonSize.Regular, leadingIcon, null, enabled)

/** Round, icon-only button with a full 44 dp touch target. [contentDescription] is required. */
@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = GraphiteTheme.colors.contentSecondary,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(GraphiteTheme.sizes.touchTarget)
            .clip(GraphiteTheme.shapes.pill)
            .pressable(onClick = onClick, enabled = enabled),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, size = GraphiteTheme.sizes.iconLarge)
    }
}
