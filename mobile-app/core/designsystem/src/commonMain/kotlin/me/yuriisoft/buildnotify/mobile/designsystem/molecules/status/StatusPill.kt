package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Tinted status pill: "✕ Ошибка", "● Идёт". */
@Composable
fun StatusPill(
    tone: BuildTone,
    label: String,
    modifier: Modifier = Modifier,
) {
    val status = GraphiteTheme.status(tone)
    Row(
        modifier = modifier.styled(rememberStatusPillStyle(tone)),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusGlyph(status, size = 12.dp, dotSize = 6.dp)
        Text(label, style = GraphiteTheme.typography.tag, color = status.main)
    }
}
