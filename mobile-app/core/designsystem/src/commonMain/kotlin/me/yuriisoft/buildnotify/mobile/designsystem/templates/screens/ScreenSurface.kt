package me.yuriisoft.buildnotify.mobile.designsystem.templates.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

/** Screen background. Put it at the root of each screen. */
@Composable
fun ScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.background(GraphiteTheme.colors.background),
            content = content,
        )
    }
}
