package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

internal val LocalGraphiteColors = staticCompositionLocalOf { GraphiteDarkColors }
internal val LocalGraphiteTypography = staticCompositionLocalOf<GraphiteTypography> {
    error("GraphiteTypography is not provided: wrap the UI in GraphiteTheme { }")
}
internal val LocalGraphiteSpacing = staticCompositionLocalOf { GraphiteSpacing() }
internal val LocalGraphiteShapes = staticCompositionLocalOf { GraphiteShapes() }
internal val LocalGraphiteSizes = staticCompositionLocalOf { GraphiteSizes() }

/** Colour that [Text][me.yuriisoft.buildnotify.mobile.designsystem.components.Text] and icons use when none is given. */
val LocalGraphiteContentColor = compositionLocalOf { Color.Unspecified }

/** Style that [Text][me.yuriisoft.buildnotify.mobile.designsystem.components.Text] uses when none is given. */
val LocalGraphiteTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * Root of every Graphite screen. Works the same on Android, iOS, desktop and web.
 *
 * ```kotlin
 * GraphiteTheme {
 *     Text("Всё собрано", style = GraphiteTheme.typography.display)
 * }
 * ```
 */
@Composable
fun GraphiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: GraphiteColors = if (darkTheme) GraphiteDarkColors else GraphiteLightColors,
    content: @Composable () -> Unit,
) {
    val typography = rememberGraphiteTypography()
    CompositionLocalProvider(
        LocalGraphiteColors provides colors,
        LocalGraphiteTypography provides typography,
        LocalGraphiteSpacing provides GraphiteSpacing(),
        LocalGraphiteShapes provides GraphiteShapes(),
        LocalGraphiteSizes provides GraphiteSizes(),
        LocalGraphiteContentColor provides colors.content,
        LocalGraphiteTextStyle provides typography.body,
        content = content,
    )
}

/** Token accessors: `GraphiteTheme.colors.failure.main`, `GraphiteTheme.typography.mono`, … */
object GraphiteTheme {
    val colors: GraphiteColors
        @Composable @ReadOnlyComposable get() = LocalGraphiteColors.current

    val typography: GraphiteTypography
        @Composable @ReadOnlyComposable get() = LocalGraphiteTypography.current

    val spacing: GraphiteSpacing
        @Composable @ReadOnlyComposable get() = LocalGraphiteSpacing.current

    val shapes: GraphiteShapes
        @Composable @ReadOnlyComposable get() = LocalGraphiteShapes.current

    val sizes: GraphiteSizes
        @Composable @ReadOnlyComposable get() = LocalGraphiteSizes.current

    /** Icon and colours of [tone] — the single source for anything status-coloured. */
    @Composable
    @ReadOnlyComposable
    fun status(tone: BuildTone): StatusStyle = statusStyleOf(tone, LocalGraphiteColors.current)
}
