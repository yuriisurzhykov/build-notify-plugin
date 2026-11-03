package me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

/** Bordered surface for secondary groups. Tappable when [onClick] is set. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val style = rememberCardStyle()
    val surface = if (onClick != null) Modifier.styledClickable(style, onClick) else Modifier.styled(style)
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.then(surface),
            verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            content = content,
        )
    }
}
