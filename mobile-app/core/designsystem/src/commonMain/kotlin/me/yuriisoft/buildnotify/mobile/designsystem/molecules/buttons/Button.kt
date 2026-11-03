package me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

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
    val typography = GraphiteTheme.typography
    val textStyle = if (size == ButtonSize.Large) typography.buttonLarge else typography.button
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
