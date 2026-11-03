package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteContentColor

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
    val appearance = rememberButtonAppearance(variant, size)
    val textStyle = if (size == ButtonSize.Large) GraphiteTheme.typography.buttonLarge else GraphiteTheme.typography.button
    val iconSize = GraphiteTheme.sizes.icon

    CompositionLocalProvider(LocalGraphiteContentColor provides appearance.content) {
        Row(
            modifier = modifier.styledClickable(appearance.style, onClick = onClick, enabled = enabled),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { Icon(it, contentDescription = null, size = iconSize) }
            Text(text = text, style = textStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailingIcon?.let { Icon(it, contentDescription = null, size = iconSize) }
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
        modifier = modifier.styledClickable(rememberIconButtonStyle(), onClick = onClick, enabled = enabled),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, size = GraphiteTheme.sizes.iconLarge)
    }
}
