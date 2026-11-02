package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic state of a build — the only reason colour appears in Graphite.
 * Every status-coloured component takes a [BuildTone] instead of raw colours.
 */
enum class BuildTone { Running, Success, Failure, Warning, Neutral }

/**
 * The colours of one [BuildTone].
 *
 * @property main      icons, dots, status text, accents.
 * @property container pills and tags: [main] at low opacity.
 * @property hero      flat background of the hero panel in this state.
 * @property onMain    content drawn on a solid [main] fill.
 */
@Immutable
data class ToneColors(
    val main: Color,
    val container: Color,
    val hero: Color,
    val onMain: Color,
)

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

private fun toneOf(main: Color, hero: Color, onMain: Color, containerAlpha: Float) = ToneColors(
    main = main,
    container = main.copy(alpha = containerAlpha),
    hero = hero,
    onMain = onMain,
)

private object ConsoleTokens {
    val console = Color(0xFF0A0C0F)
    val content = Color(0xFFC9CED6)
    val muted = Color(0xFF868D98)
    val gutter = Color(0xFF5F6672)
    val warning = Color(0xFFF2B84B)
    val error = Color(0xFFFF7A6B)
}

val GraphiteDarkColors: GraphiteColors = GraphiteColors(
    isDark = true,
    background = Color(0xFF0E1013),
    surface = Color(0xFF171A1F),
    surfaceRaised = Color(0xFF262B33),
    scrim = Color(0xB807080A),
    line = Color(0xFF2A2F37),
    lineStrong = Color(0xFF3A404A),
    content = Color(0xFFE8EAED),
    contentSecondary = Color(0xFFA3A9B3),
    contentTertiary = Color(0xFF868D98),
    inverse = Color(0xFFE8EAED),
    onInverse = Color(0xFF0E1013),
    console = ConsoleTokens.console,
    consoleContent = ConsoleTokens.content,
    consoleMuted = ConsoleTokens.muted,
    consoleGutter = ConsoleTokens.gutter,
    consoleWarning = ConsoleTokens.warning,
    consoleError = ConsoleTokens.error,
    running = toneOf(Color(0xFF6CA8FF), hero = Color(0xFF141A24), onMain = Color(0xFF0E1013), containerAlpha = 0.14f),
    success = toneOf(Color(0xFF5BD39A), hero = Color(0xFF151B18), onMain = Color(0xFF0E1013), containerAlpha = 0.14f),
    failure = toneOf(Color(0xFFFF7A6B), hero = Color(0xFF211517), onMain = Color(0xFF0E1013), containerAlpha = 0.14f),
    warning = toneOf(Color(0xFFF2B84B), hero = Color(0xFF201B12), onMain = Color(0xFF0E1013), containerAlpha = 0.14f),
    neutral = toneOf(Color(0xFFA3A9B3), hero = Color(0xFF171A1F), onMain = Color(0xFF0E1013), containerAlpha = 0.12f),
)

val GraphiteLightColors: GraphiteColors = GraphiteColors(
    isDark = false,
    background = Color(0xFFF5F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFECEEF2),
    scrim = Color(0x7A0E1013),
    line = Color(0xFFDFE2E7),
    lineStrong = Color(0xFFC6CBD3),
    content = Color(0xFF14171C),
    contentSecondary = Color(0xFF4C5360),
    contentTertiary = Color(0xFF656C78),
    inverse = Color(0xFF14171C),
    onInverse = Color(0xFFFFFFFF),
    console = Color(0xFF12151A),
    consoleContent = ConsoleTokens.content,
    consoleMuted = ConsoleTokens.muted,
    consoleGutter = ConsoleTokens.gutter,
    consoleWarning = ConsoleTokens.warning,
    consoleError = ConsoleTokens.error,
    running = toneOf(Color(0xFF2463D8), hero = Color(0xFFE9F0FC), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
    success = toneOf(Color(0xFF1D8350), hero = Color(0xFFE6F4EC), onMain = Color(0xFFFFFFFF), containerAlpha = 0.12f),
    failure = toneOf(Color(0xFFC93A2C), hero = Color(0xFFFBEAE7), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
    warning = toneOf(Color(0xFF9A6200), hero = Color(0xFFFAF1E0), onMain = Color(0xFFFFFFFF), containerAlpha = 0.12f),
    neutral = toneOf(Color(0xFF4C5360), hero = Color(0xFFFFFFFF), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
)
