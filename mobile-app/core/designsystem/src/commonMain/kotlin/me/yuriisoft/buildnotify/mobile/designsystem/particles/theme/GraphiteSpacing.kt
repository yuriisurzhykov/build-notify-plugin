package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 4 pt spacing scale, the two half steps the components use, and the layout constants
 * every screen uses. Read through [GraphiteTheme.spacing]; never write a `dp` literal.
 */
@Immutable
data class GraphiteSpacing(
    /** Between lines that belong together visually (title and its meta). */
    val hairline: Dp = 1.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    /** Between an inline glyph and its label, or tightly related lines. */
    val inline: Dp = 6.dp,
    val s: Dp = 8.dp,
    /** Between the items of one group: hero lines, PIN digit groups, a log offset and its text. */
    val group: Dp = 10.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    /** Horizontal padding of screen content. */
    val screen: Dp = 20.dp,
    /** Inset of the hero panel from the screen edge. */
    val heroInset: Dp = 8.dp,
    /** Vertical gap between major sections. */
    val section: Dp = 28.dp,
)
