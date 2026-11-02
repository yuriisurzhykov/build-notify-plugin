package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** Default glyph of a tone; `null` means "draw a dot" (running, neutral). */
fun BuildTone.icon(): ImageVector? = when (this) {
    BuildTone.Success -> GraphiteIcons.Check
    BuildTone.Failure -> GraphiteIcons.Close
    BuildTone.Warning -> GraphiteIcons.Minus
    BuildTone.Running,
    BuildTone.Neutral -> null
}

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
    Box(
        modifier
            .size(size)
            .background(GraphiteTheme.colors.tone(tone).main, GraphiteTheme.shapes.pill)
            .then(described),
    )
}

/** Tinted status pill: "✕ Ошибка", "● Идёт". */
@Composable
fun StatusPill(
    tone: BuildTone,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = GraphiteTheme.colors.tone(tone)
    Row(
        modifier = modifier
            .height(GraphiteTheme.sizes.pill)
            .background(colors.container, GraphiteTheme.shapes.pill)
            .padding(start = 8.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val glyph = tone.icon()
        if (glyph != null) {
            Icon(glyph, contentDescription = null, tint = colors.main, size = 12.dp)
        } else {
            StatusDot(tone, size = 6.dp)
        }
        Text(label, style = GraphiteTheme.typography.caption.copy(fontWeight = GraphiteTheme.typography.label.fontWeight), color = colors.main)
    }
}

/**
 * Inline status line at the top of a hero: coloured glyph + label,
 * e.g. "✕ Сборка упала · 2 мин назад".
 */
@Composable
fun StatusLabel(
    tone: BuildTone,
    text: String,
    modifier: Modifier = Modifier,
) {
    val main = GraphiteTheme.colors.tone(tone).main
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val glyph = tone.icon()
        if (glyph != null) Icon(glyph, contentDescription = null, tint = main, size = GraphiteTheme.sizes.iconSmall)
        else StatusDot(tone)
        Text(text, style = GraphiteTheme.typography.label, color = main)
    }
}

/** Large round status badge for the hero ("✓" on "Всё собрано"). */
@Composable
fun StatusBadge(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = GraphiteTheme.colors.tone(tone)
    Box(
        modifier = modifier
            .size(GraphiteTheme.sizes.touchTarget)
            .background(colors.container, GraphiteTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        val glyph = tone.icon()
        if (glyph != null) Icon(glyph, contentDescription, tint = colors.main, size = GraphiteTheme.sizes.iconLarge)
        else StatusDot(tone, size = 12.dp, contentDescription = contentDescription)
    }
}

/** Small source tag in list rows — "агент". */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: BuildTone = BuildTone.Running,
) {
    val colors = GraphiteTheme.colors.tone(tone)
    Text(
        text = text,
        modifier = modifier
            .background(colors.container, GraphiteTheme.shapes.tag)
            .padding(horizontal = 6.dp, vertical = 1.dp),
        style = GraphiteTheme.typography.caption.copy(fontWeight = GraphiteTheme.typography.label.fontWeight),
        color = colors.main,
    )
}

/** "● MyProject · Android Studio" — connection state in the top bar. */
@Composable
fun ConnectionBadge(
    text: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(if (connected) BuildTone.Success else BuildTone.Neutral, size = 7.dp)
        Text(text, style = GraphiteTheme.typography.meta, color = GraphiteTheme.colors.contentSecondary, maxLines = 1)
    }
}
