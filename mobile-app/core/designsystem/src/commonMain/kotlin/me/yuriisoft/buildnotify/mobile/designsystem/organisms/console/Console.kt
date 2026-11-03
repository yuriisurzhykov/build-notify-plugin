package me.yuriisoft.buildnotify.mobile.designsystem.organisms.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteTextStyle

/**
 * Dark mono block for build output. Stays dark in the light theme too.
 * [translucent] lets a hero tint show through (live tail inside a hero).
 */
@Composable
fun Console(
    modifier: Modifier = Modifier,
    translucent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GraphiteTheme.colors
    CompositionLocalProvider(
        LocalGraphiteContentColor provides colors.consoleContent,
        LocalGraphiteTextStyle provides GraphiteTheme.typography.log,
    ) {
        Column(
            modifier = modifier.fillMaxWidth().styled(rememberConsoleStyle(translucent)),
            verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xxs),
            content = content,
        )
    }
}

@Composable
internal fun rememberConsoleStyle(translucent: Boolean): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.inset
    val spacing = GraphiteTheme.spacing
    val translucentAlpha = GraphiteTheme.opacity.translucentConsole
    return remember(colors, surfaceShape, spacing, translucentAlpha, translucent) {
        Style {
            shape(surfaceShape)
            background(if (translucent) colors.console.copy(alpha = translucentAlpha) else colors.console)
            contentPaddingHorizontal(spacing.s)
            contentPaddingVertical(spacing.m)
        }
    }
}
