package me.yuriisoft.buildnotify.mobile.designsystem.atoms.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** 8 dp status dot for dense lists. Give [contentDescription] unless the row says the status in text. */
@Composable
fun StatusDot(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = GraphiteTheme.sizes.statusDot,
) {
    val described = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Box(modifier.styled(rememberStatusDotStyle(tone, size)).then(described))
}

/** Solid dot in the tone's main colour. */
@Composable
internal fun rememberStatusDotStyle(tone: BuildTone, diameter: Dp): Style {
    val status = GraphiteTheme.status(tone)
    val pill = GraphiteTheme.shapes.pill
    return remember(status, pill, diameter) {
        Style {
            size(diameter)
            shape(pill)
            background(status.main)
        }
    }
}
