package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Large round status badge for the hero ("✓" on "Всё собрано"). */
@Composable
fun StatusBadge(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val status = GraphiteTheme.status(tone)
    Box(
        modifier = modifier.styled(rememberToneCircleStyle(status, GraphiteTheme.sizes.touchTarget)),
        contentAlignment = Alignment.Center,
    ) {
        StatusGlyph(
            status,
            size = GraphiteTheme.sizes.iconLarge,
            dotSize = 12.dp,
            contentDescription = contentDescription,
        )
    }
}
