package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.StatusStyle

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

/** Small source tag in list rows — "агент". */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    tone: BuildTone = BuildTone.Running,
) {
    val status = GraphiteTheme.status(tone)
    Text(
        text = text,
        modifier = modifier.styled(rememberTagStyle(tone)),
        style = GraphiteTheme.typography.tag,
        color = status.main,
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

/** The status glyph, or a dot for tones without one — the only place that choice is made. */
@Composable
private fun StatusGlyph(
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
