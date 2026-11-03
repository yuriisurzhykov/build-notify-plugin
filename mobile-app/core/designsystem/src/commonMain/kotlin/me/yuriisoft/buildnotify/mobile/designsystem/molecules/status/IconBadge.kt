package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Round badge with an arbitrary [icon] in the colours of [tone]; the colours glide when
 * [tone] changes. Used for the big glyphs of multi-step flows.
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    tone: BuildTone,
    modifier: Modifier = Modifier,
    size: IconBadgeSize = IconBadgeSize.Regular,
    contentDescription: String? = null,
) {
    val status = animateStatus(tone)
    val metrics = GraphiteTheme.dimens.status
    val badge = when (size) {
        IconBadgeSize.Regular -> metrics.iconBadge
        IconBadgeSize.Small   -> metrics.iconBadgeSmall
    }
    Box(
        modifier = modifier.styled(rememberToneCircleStyle(status, badge.diameter)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = status.main, size = badge.icon)
    }
}
