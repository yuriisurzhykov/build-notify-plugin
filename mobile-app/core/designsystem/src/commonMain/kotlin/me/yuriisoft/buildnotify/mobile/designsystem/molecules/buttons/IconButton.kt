package me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

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
