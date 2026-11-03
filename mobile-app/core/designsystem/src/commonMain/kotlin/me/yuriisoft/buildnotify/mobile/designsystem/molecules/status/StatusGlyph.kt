package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.status.StatusDot
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.StatusStyle

/** The status glyph, or a dot for tones without one — the only place that choice is made. */
@Composable
internal fun StatusGlyph(
    status: StatusStyle,
    size: Dp,
    dotSize: Dp,
    contentDescription: String? = null,
) {
    val icon = status.icon
    if (icon != null) {
        Icon(icon, contentDescription = contentDescription, tint = status.main, size = size)
    } else {
        StatusDot(status.tone, size = dotSize, contentDescription = contentDescription)
    }
}
