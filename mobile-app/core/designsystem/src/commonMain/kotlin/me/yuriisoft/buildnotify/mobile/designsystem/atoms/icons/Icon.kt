package me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

/**
 * Tinted vector icon. Pass [contentDescription] = `null` for decorative icons
 * next to a text label; give one for icons that carry meaning on their own.
 */
@Composable
fun Icon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalGraphiteContentColor.current,
    size: Dp = GraphiteTheme.sizes.icon,
) {
    val painter = rememberVectorPainter(icon)
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }
    Box(
        modifier
            .size(size)
            .paint(
                painter = painter,
                colorFilter = if (tint.isSpecified) ColorFilter.tint(tint) else null,
                contentScale = ContentScale.Fit,
            )
            .then(semantics),
    )
}
