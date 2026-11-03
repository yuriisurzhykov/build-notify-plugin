package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Graphite colour tokens.
 *
 * Statuses differ in lightness and icon, not only in hue. The console stays dark
 * in both schemes — logs are read the same way everywhere — so it has its own
 * content colours.
 */
@Immutable
data class GraphiteColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val scrim: Color,
    val line: Color,
    val lineStrong: Color,
    val content: Color,
    val contentSecondary: Color,
    val contentTertiary: Color,
    val inverse: Color,
    val onInverse: Color,
    val console: Color,
    val consoleContent: Color,
    val consoleMuted: Color,
    val consoleGutter: Color,
    val consoleWarning: Color,
    val consoleError: Color,
    val running: ToneColors,
    val success: ToneColors,
    val failure: ToneColors,
    val warning: ToneColors,
    val neutral: ToneColors,
) {
    fun tone(tone: BuildTone): ToneColors = when (tone) {
        BuildTone.Running -> running
        BuildTone.Success -> success
        BuildTone.Failure -> failure
        BuildTone.Warning -> warning
        BuildTone.Neutral -> neutral
    }
}
