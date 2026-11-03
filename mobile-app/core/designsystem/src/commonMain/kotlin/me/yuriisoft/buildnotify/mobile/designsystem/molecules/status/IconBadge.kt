package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone

/**
 * Round badge with an arbitrary [icon] in the colours of [tone]; the colours glide when
 * [tone] changes. Used for the big glyphs of multi-step flows.
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    tone: BuildTone,
    modifier: Modifier = Modifier,
    diameter: Dp = IconBadgeDiameter,
    iconSize: Dp = IconBadgeIconSize,
    contentDescription: String? = null,
) {
    val status = animateStatus(tone)
    Box(
        modifier = modifier.styled(rememberToneCircleStyle(status, diameter)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = status.main, size = iconSize)
    }
}

private val IconBadgeDiameter = 76.dp

private val IconBadgeIconSize = 32.dp
