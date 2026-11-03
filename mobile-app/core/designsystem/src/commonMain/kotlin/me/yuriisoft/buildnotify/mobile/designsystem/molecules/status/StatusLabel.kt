package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Inline status line at the top of a hero: coloured glyph + label,
 * e.g. "✕ Build failed · 2 min ago".
 */
@Composable
fun StatusLabel(
    tone: BuildTone,
    text: String,
    modifier: Modifier = Modifier,
) {
    val status = GraphiteTheme.status(tone)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusGlyph(status, size = GraphiteTheme.sizes.iconSmall, dotSize = GraphiteTheme.sizes.statusDot)
        Text(text, style = GraphiteTheme.typography.label, color = status.main)
    }
}
