package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Tinted status pill: "✕ Failed", "● Running". */
@Composable
fun StatusPill(
    tone: BuildTone,
    label: String,
    modifier: Modifier = Modifier,
) {
    val status = GraphiteTheme.status(tone)
    val metrics = GraphiteTheme.dimens.status
    Row(
        modifier = modifier.styled(rememberStatusPillStyle(tone)),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.inline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusGlyph(status, size = metrics.pillGlyph, dotSize = metrics.pillDot)
        Text(label, style = GraphiteTheme.typography.tag, color = status.main)
    }
}
