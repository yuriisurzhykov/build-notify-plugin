package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

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
internal val LocalGraphiteDimens = staticCompositionLocalOf { GraphiteDimens() }
internal val LocalGraphiteMotion = staticCompositionLocalOf { GraphiteMotion() }
internal val LocalGraphiteOpacity = staticCompositionLocalOf { GraphiteOpacity() }

/** Colour that [Text][me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text] and icons use when none is given. */
val LocalGraphiteContentColor = compositionLocalOf { Color.Unspecified }

/** Style that [Text][me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text] uses when none is given. */
val LocalGraphiteTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * Root of every Graphite screen. Works the same on Android, iOS, desktop and web.
 * Every token is provided here once; components read them only through the
 * [GraphiteTheme] object, never through literals or the composition locals.
 *
 * ```kotlin
 * GraphiteTheme {
 *     Text(stringResource(Res.string.all_built), style = GraphiteTheme.typography.display)
 * }
 * ```
 */
@Composable
fun GraphiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: GraphiteColors = if (darkTheme) GraphiteDarkColors else GraphiteLightColors,
    spacing: GraphiteSpacing = GraphiteSpacing(),
    sizes: GraphiteSizes = GraphiteSizes(),
    dimens: GraphiteDimens = GraphiteDimens(),
    shapes: GraphiteShapes = GraphiteShapes(),
    motion: GraphiteMotion = GraphiteMotion(),
    opacity: GraphiteOpacity = GraphiteOpacity(),
    content: @Composable () -> Unit,
) {
    val typography = rememberGraphiteTypography()
    CompositionLocalProvider(
        LocalGraphiteColors provides colors,
        LocalGraphiteTypography provides typography,
        LocalGraphiteSpacing provides spacing,
        LocalGraphiteShapes provides shapes,
        LocalGraphiteSizes provides sizes,
        LocalGraphiteDimens provides dimens,
        LocalGraphiteMotion provides motion,
        LocalGraphiteOpacity provides opacity,
        LocalGraphiteContentColor provides colors.content,
        LocalGraphiteTextStyle provides typography.body,
        content = content,
    )
}

/**
 * The single entry point to Graphite tokens: `GraphiteTheme.colors.failure.main`,
 * `GraphiteTheme.spacing.l`, `GraphiteTheme.dimens.status.pillGlyph`,
 * `GraphiteTheme.motion.enter()`, …
 */
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

    /** Fixed metrics of Graphite components, by component family. */
    val dimens: GraphiteDimens
        @Composable @ReadOnlyComposable get() = LocalGraphiteDimens.current

    val motion: GraphiteMotion
        @Composable @ReadOnlyComposable get() = LocalGraphiteMotion.current

    val opacity: GraphiteOpacity
        @Composable @ReadOnlyComposable get() = LocalGraphiteOpacity.current

    /** Icon and colours of [tone] — the single source for anything status-coloured. */
    @Composable
    @ReadOnlyComposable
    fun status(tone: BuildTone): StatusStyle = statusStyleOf(tone, LocalGraphiteColors.current)
}
