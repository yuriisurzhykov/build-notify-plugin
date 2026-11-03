package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons

/**
 * Everything a status looks like, resolved in one place: the glyph and every colour.
 *
 * Obtain it with [GraphiteTheme.status]; never pick a status icon or colour by hand.
 * Adding a tone means adding one branch here, and every pill, badge, dot and hero
 * follows automatically.
 *
 * @property icon glyph for the status, or `null` when the status is drawn as a dot
 *   (running, neutral).
 */
@Immutable
data class StatusStyle(
    val tone: BuildTone,
    val icon: ImageVector?,
    val colors: ToneColors,
) {
    val main: Color get() = colors.main
    val container: Color get() = colors.container
    val hero: Color get() = colors.hero
    val onMain: Color get() = colors.onMain
}

internal fun statusStyleOf(tone: BuildTone, colors: GraphiteColors): StatusStyle = StatusStyle(
    tone = tone,
    icon = when (tone) {
        BuildTone.Success -> GraphiteIcons.Check
        BuildTone.Failure -> GraphiteIcons.Close
        BuildTone.Warning -> GraphiteIcons.Minus
        BuildTone.Running,
        BuildTone.Neutral -> null
    },
    colors = colors.tone(tone),
)
