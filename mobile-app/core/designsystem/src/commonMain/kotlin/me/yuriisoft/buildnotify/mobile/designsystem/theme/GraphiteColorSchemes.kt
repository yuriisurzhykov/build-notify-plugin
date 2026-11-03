package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.ui.graphics.Color

private fun toneOf(main: Color, hero: Color, onMain: Color, containerAlpha: Float) = ToneColors(
    main = main,
    container = main.copy(alpha = containerAlpha),
    hero = hero,
    onMain = onMain,
)

// The console stays dark in both schemes, so its content colours are shared.
private val ConsoleContent = Color(0xFFC9CED6)
private val ConsoleMuted = Color(0xFF868D98)
private val ConsoleGutter = Color(0xFF5F6672)
private val ConsoleWarning = Color(0xFFF2B84B)
private val ConsoleError = Color(0xFFFF7A6B)

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
    console = Color(0xFF0A0C0F),
    consoleContent = ConsoleContent,
    consoleMuted = ConsoleMuted,
    consoleGutter = ConsoleGutter,
    consoleWarning = ConsoleWarning,
    consoleError = ConsoleError,
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
    consoleContent = ConsoleContent,
    consoleMuted = ConsoleMuted,
    consoleGutter = ConsoleGutter,
    consoleWarning = ConsoleWarning,
    consoleError = ConsoleError,
    running = toneOf(Color(0xFF2463D8), hero = Color(0xFFE9F0FC), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
    success = toneOf(Color(0xFF1D8350), hero = Color(0xFFE6F4EC), onMain = Color(0xFFFFFFFF), containerAlpha = 0.12f),
    failure = toneOf(Color(0xFFC93A2C), hero = Color(0xFFFBEAE7), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
    warning = toneOf(Color(0xFF9A6200), hero = Color(0xFFFAF1E0), onMain = Color(0xFFFFFFFF), containerAlpha = 0.12f),
    neutral = toneOf(Color(0xFF4C5360), hero = Color(0xFFFFFFFF), onMain = Color(0xFFFFFFFF), containerAlpha = 0.10f),
)
